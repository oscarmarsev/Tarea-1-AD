package org.izv.aff.consultaagendaad;

import static android.content.ContentValues.TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.UserDictionary;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.izv.aff.consultaagendaad.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private final int CONTACT_PERMISSION = 1;

    // Creamos variables
    private Button bt_search; // = findViewById(R.id.bt_search);
    private EditText etPhone;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // si hace return true es que este metodo se encarga de hacer algo si no lo hara otra clase u otro metodo.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // https://www.develou.com/como-crear-actividad-preferencias-android/#Crear_Una_Actividad_De_Preferencias_En_Android
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void initialize() {
        bt_search = findViewById(R.id.bt_search);
        etPhone = findViewById(R.id.etPhone);
        tvResult = findViewById(R.id.tvResult);

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchIfPermitted();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void explain() {
        showRationaleDialog(getString(R.string.title),
                                      getString(R.string.message),
                                      Manifest.permission.READ_CONTACTS,
                                      CONTACT_PERMISSION);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_PERMISSION);
    }

    private void search() {
        tvResult.setText("A PELO YA SABES");
        // Buscar entre los contactos
        // ContentProvider, Proveedor de contenidos
        // ContentResolver, Consultor de contenidos. con este se consulta al provider.
        // Queries the user dictionary and returns results
        /* Cursor cursor = getContentResolver().query( //
                UserDictionary.Words.CONTENT_URI,             // The content URI of the words table
                new String[] {"projection"},                  // The columns to return for each row
                "selectionClause",                            // Selection criteria
                new String[] {"selectionArgs"},               // Selection criteria
                "sortOrder");*/                               // The sort order for the returned rows
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String proyeccion[] = new String[] {ContactsContract.Contacts.DISPLAY_NAME}; // null es todos los campos, el *
        String seleccion = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ? and " +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
        String argumentos[] = new String[]{"1","1"};
        seleccion = null;
        argumentos = null;
        String orden = ContactsContract.Contacts.DISPLAY_NAME + " collate localized asc";
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        String[] columnas = cursor.getColumnNames();
        for (String s: columnas) {
            Log.v("zzz", s);
        }
        String displayName;
        int columna = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        while(cursor.moveToNext()) {
            displayName = cursor.getString(columna);
            Log.v("zzz", displayName);
        }
        // Tarea
        Uri uri2 = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String proyeccion2[] = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        String seleccion2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " like ?";
        String argumentos2[] = new String[]{"1%2%3"}; // Argumento para buscar por numeros introducidos, tarea.
        String orden2 = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        Cursor cursor2 = getContentResolver().query(uri2, proyeccion2, seleccion2, argumentos2, orden2);
        String[] columnas2 = cursor2.getColumnNames();
        for (String s: columnas2) {
            Log.v("zzzy", s);
        }
        String nombre, numero;
        int columnaNombre = cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int columnaNumero = cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        while(cursor2.moveToNext()) {
            nombre = cursor2.getString(columnaNombre);
            numero = cursor2.getString(columnaNumero);

            Log.v("zzzy", nombre + ": " + numero);
            for (String s: columnas2) {
                int pos = cursor2.getColumnIndex(s);
                String valor = cursor2.getString(pos);
                Log.v("zzzy", pos + " " + s + " " + valor);
            }
        }
    }

    private void searchIfPermitted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // La version de android es posterior a la 6 incluida
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Ya tengo el permiso
                search();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                explain();
            } else {
                requestPermission();
            }
        } else {
            // La version de android es anterior a la 6
            // Ya tengo el permiso
            search();
        }
    }

    private void showRationaleDialog(String title, String message, String permission, int requestCode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Si pulso negativo no quiero hacer nada
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Si pulso positivo quiero pedir los permisos
                        requestPermission();
                    }
                });


        builder.create().show();

    }

}