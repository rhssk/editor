////////////////////////////////////////////////////////////////////////////////
//
//  Editor - Text editor for Android
//
//  Copyright © 2017  Bill Farmer
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
////////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Editor extends Activity
{
    public final static String TAG = "Editor";

    private final static int BUFFER_SIZE = 1024;
    private final static int GET_TEXT = 0;

    private File file = null;
    private EditText textView;

    private boolean dirty = false;

    // onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);

        textView = (EditText) findViewById(R.id.text);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);

            String title = uri.getLastPathSegment();
            setTitle(title);

            file = new File(uri.getPath());
            String text = read(file);
            textView.setText(text);
        }

        setListeners();
    }

    // setListeners
    private void setListeners()
    {

        if (textView != null)
            textView.addTextChangedListener(new TextWatcher()
        {
            // afterTextChanged
            @Override
            public void afterTextChanged (Editable s)
            {
                dirty = true;
            }

            // beforeTextChanged
            @Override
            public void beforeTextChanged (CharSequence s,
                                           int start,
                                           int count,
                                           int after) {}
            // onTextChanged
            @Override
            public void onTextChanged (CharSequence s,
                                       int start,
                                       int before,
                                       int count) {}
        });
    }

    // onCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    // onPrepareOptionsMenu
    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
        menu.findItem(R.id.open).setVisible (file == null);
        menu.findItem(R.id.save).setVisible (dirty);

        return true;
    }

    // onOptionsItemSelected
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            onBackPressed();
            break;
        case R.id.open:
            openFile();
            break;
        case R.id.save:
            saveFile();
            break;
        default:
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    // onBackPressed
    @Override
    public void onBackPressed()
    {
        if (dirty)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.appName);
            builder.setMessage(R.string.modified);

            // Add the buttons
            builder.setPositiveButton(R.string.ok, new
                                      DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        finish();
                    }
                });
            builder.setNegativeButton(R.string.cancel, new
                                      DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // User cancelled the dialog
                    }
                });

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        else
            finish();
    }

    // onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        // Do nothing if cancelled
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode)
        {
        case GET_TEXT:
            Uri uri = data.getData();
            readFile(uri);
            break;
        }
    }

    // openFile
    private void openFile()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        startActivityForResult(Intent.createChooser(intent, null), GET_TEXT);
    }

    // readFile
    private void readFile(Uri uri)
    {
        String title = uri.getLastPathSegment();
        setTitle(title);

        file = new File(uri.getPath());
        String text = read(file);
        textView.setText(text);
    }

    // saveFile
    private void saveFile()
    {
        String text = textView.getText().toString();
        write(text, file);
    }

    // read
    private static String read(File file)
    {
        StringBuilder text = new StringBuilder();
        try
        {
            FileReader fileReader = new FileReader(file);
            char buffer[] = new char[BUFFER_SIZE];
            int n;
            while ((n = fileReader.read(buffer)) != -1)
                text.append(String.valueOf(buffer, 0, n));
            fileReader.close();
        }

        catch (Exception e) {}

        return text.toString();
    }

    // write
    private void write(String text, File file)
    {
        file.getParentFile().mkdirs();
        try
        {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(text);
            fileWriter.close();
        }
        catch (Exception e) {}
    }
}
