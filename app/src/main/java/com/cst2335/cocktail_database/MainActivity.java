package com.cst2335.cocktail_database;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/*
    Your application should have an EditText for entering the name of a drink. There should also be a “search”
button which sends the search term to the server and returns a list of drinks that match that name.

    The URL for searching is “https://www.thecocktaildb.com/api/json/v1/1/search.php?s=....”
where … is the name that the user typed in.

    Your application must show a list of results in a RecyclerView, with each row showing a different result returned.

    If the user selects a drink from the list, your application will show the thumbnail picture,
the instructions for making the drink, and the first 3 ingredients from the list of ingredients.

    Also in the detail fragment, the user can save the data to the device for offline viewing.
The user must be able to view a list of their saved data and remove them from the database of favourites if they choose.

The SharedPreferences should save the user’s search words so that the next time you start the application,
the previous search term is shown.
*/

public class MainActivity extends AppCompatActivity {

    //FG
    public static final String ITEM_SELECTED = "ITEM";
    public static final String ITEM_POSITION = "POSITION";
    public static final String ITEM_ID = "ID";


    //    ArrayList<String> pic = new ArrayList<>();
    ArrayList<Contact> contactsList = new ArrayList<>();
    //    ArrayAdapter <String> arrayAdapter;
    MyOwnAdapter myAdapter;

    SQLiteDatabase db;
    private static int ACTIVITY_VIEW_CONTACT = 33;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
        EditText nameEdit = findViewById(R.id.etSearch);
        Button clickBtnSearch = findViewById(R.id.btnSearch);
        ListView list = (ListView) findViewById(R.id.listDrink);
        Button clickBtnFravor = findViewById(R.id.btnFavorites);


        myAdapter = new MyOwnAdapter();
        list.setAdapter(myAdapter);


        list.setOnItemClickListener((list1, item, position, id) -> {
            //Create a bundle to pass data to the new fragment
            Bundle dataToPass = new Bundle();
            dataToPass.putString(ITEM_SELECTED, contactsList.get(position).name);
            dataToPass.putInt(ITEM_POSITION, position);
            dataToPass.putLong(ITEM_ID, id);


            Intent nextActivity = new Intent(MainActivity.this, DrinkOnClickActivity.class);
            nextActivity.putExtras(dataToPass); //send data to next activity
            // passing drink name from the row
            nextActivity.putExtra("keywd", contactsList.get(position).name);
            startActivity(nextActivity); //make the transition

        });


        loadDataFromDatabase();

        myAdapter = new MyOwnAdapter();
        list.setAdapter(myAdapter);

        //database
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            showContact(position);
            return false;
        });

        SharedPreferences sp = getSharedPreferences("prefernce", MODE_PRIVATE);
        String searchPref= sp.getString("nameEdit", null);
        nameEdit.setText(searchPref);

//        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
//                android.R.layout.simple_list_item_1, pic);


        clickBtnSearch.setOnClickListener(click -> {
            //
            String name = nameEdit.getText().toString();

            SharedPreferences.Editor editor = sp.edit();
            //name = search.getText().toString();
            editor.putString("nameEdit",name);bee
            editor.commit();


            //add to the database and get the new ID
            ContentValues newRowValues = new ContentValues();

            //Now provide a value for every database column defined in MyOpener.java:
            //put string name in the NAME column:
            newRowValues.put(MyOpener.COL_NAME, name);

            long newId = db.insert(MyOpener.TABLE_NAME, null, newRowValues);

            Contact newContact = new Contact(name, newId);

            contactsList.add(newContact);

            myAdapter.notifyDataSetChanged();

            nameEdit.setText("");

            Toast.makeText(this, "Inserted item id:" + newId, Toast.LENGTH_LONG).show();
//            if(editSearch != null)
//                editSearch = editSearch.replaceAll("\\s","+");
//         //
////            contactsList.add(newContact);
//            pic.add(editSearch);
//            list.setAdapter(arrayAdapter);
//            arrayAdapter.notifyDataSetChanged();
        });

        //Intent favoritePage = new Intent (this, FavoriteDrinks.class);
        clickBtnFravor.setOnClickListener(click -> {

            startActivity(new Intent (this, FavoriteDrinks.class));
        });

    }


    //database
    private void loadDataFromDatabase() {
        //get a database connection:
        MyOpener dbOpener = new MyOpener(this);
        db = dbOpener.getWritableDatabase(); //This calls onCreate() if you've never built the table before, or onUpgrade if the version here is newer


        // We want to get all of the columns. Look at MyOpener.java for the definitions:
        java.lang.String[] columns = {MyOpener.COL_ID, MyOpener.COL_NAME};
        //query all the results from the database:
        Cursor results = db.query(false, MyOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        //Now the results object has rows of results that match the query.
        //find the column indices:

        int nameColIndex = results.getColumnIndex(MyOpener.COL_NAME);
        int idColIndex = results.getColumnIndex(MyOpener.COL_ID);

        //iterate over the results, return true if there is a next item:
        while (results.moveToNext()) {
            String name = results.getString(nameColIndex);
            long id = results.getLong(idColIndex);

            //add the new Contact to the array list:
            contactsList.add(new Contact(name, id));
        }
    }


    protected void showContact(int position) {

        Contact selectedContact = contactsList.get(position);
        View contact_view = getLayoutInflater().inflate(R.layout.contact_edit, null);
        //get the TextViews
        EditText rowName = contact_view.findViewById(R.id.row_name);
        TextView rowId = contact_view.findViewById(R.id.row_id);

        //set the fields for the alert dialog
        rowName.setText(selectedContact.getName());
        rowId.setText("id:" + selectedContact.getId());


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("You clicked on item #" + position).setMessage("You can update the fields and then click update to save in the database").setView(contact_view) //add the 3 edit texts showing the contact information
                .setPositiveButton("Update", (click, b) -> {
                    selectedContact.update(rowName.getText().toString());
//                    updateContact(selectedContact);
                    myAdapter.notifyDataSetChanged(); //the email and name have changed so rebuild the list
                }).setNegativeButton("Delete", (click, b) -> {
                    deleteContact(selectedContact); //remove the contact from database
                    contactsList.remove(position); //remove the contact from contact list
                    myAdapter.notifyDataSetChanged(); //there is one less item so update the list
                }).setNeutralButton("dismiss", (click, b) -> {
                }).create().show();
        //
    }


//    protected void updateContact(Contact c)
//    {
//        //Create a ContentValues object to represent a database row:
//        ContentValues updatedValues = new ContentValues();
//        updatedValues.put(MyOpener.COL_NAME, c.getName());
//        db.update(MyOpener.TABLE_NAME, updatedValues, MyOpener.COL_ID + "= ?", new String[] {Long.toString(c.getId())});
//    }

    protected void deleteContact(Contact c) {
        db.delete(MyOpener.TABLE_NAME, MyOpener.COL_ID + "= ?", new String[]{Long.toString(c.getId())});
    }


    protected class MyOwnAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return contactsList.size();
        }

        public Contact getItem(int position) {
            return contactsList.get(position);
        }

        public View getView(int position, View old, ViewGroup parent) {
            View newView = getLayoutInflater().inflate(R.layout.contact_row, parent, false);

            Contact thisRow = getItem(position);

            //get the TextViews
            TextView rowName = (TextView) newView.findViewById(R.id.row_name);
            TextView rowId = (TextView) newView.findViewById(R.id.row_id);

            //update the text fields:
            rowName.setText(thisRow.getName());

            rowId.setText("id:" + thisRow.getId());

            //return the row:
            return newView;
        }

        //last week we returned (long) position. Now we return the object's database id that we get from line 71
        public long getItemId(int position) {
            return getItem(position).getId();
        }
    }

    //set up the toolbar menu
    public boolean onCreateOptionsMenu(Menu menu){

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    //set up the toolbar menu, create a alert dialog for item click
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.help_item:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("How to Use")
                           .setMessage("Please enter a drink you like and click on search to find out more! ");
                alertDialog.show();

        }

        return true;
        }




    }

    /*
    private class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return pic.size();
        }

        @Override
        public Object getItem(int i) {
            return pic.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater inflater = getLayoutInflater();
            View newView;
            TextView tView;

            newView = inflater.inflate(R.layout.listDrink, viewGroup, false);
            tView = newView.findViewById(R.id.etSearch);
            tView.setText(getItem(i).toString());

            return newView;
        }
    }

     */
