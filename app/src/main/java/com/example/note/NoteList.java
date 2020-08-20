/**
 * @author Amol Vaze
 *
 */
package com.example.note;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/*
*
* @author Preeti Sharma
 */
public class NoteList extends ListActivity {
	
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_DELETE=2;
	
    private static final int CANCEL_ID = Menu.FIRST;
	private int mNoteNumber = 1;
	
	private NotesDbAdapter mDbHelper;


     /*
     *
     * onCreate method,starting point to launch an activity
     */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notelist);
		mDbHelper = new NotesDbAdapter (this);
		mDbHelper.open();
		fillData();				
		registerForContextMenu(getListView());
		Button addnote = (Button)findViewById(R.id.addnotebutton);
		addnote.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				createNote();
				}
		});
		
	}

    //Loading the menu items for this activity
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notelist_menu, menu);
        menu.add(0, R.id.exitId, 0, R.string.exit);
		return true;		
	}

    //overriding onOptionsItemSelected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_about:
	          
	           AlertDialog.Builder dialog = new AlertDialog.Builder(NoteList.this);
	           dialog.setTitle("About");
	           dialog.setMessage("Demo for assignment3,UTD "
	        		   );
	           dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
	        	   @Override
	        	   public void onClick(DialogInterface dialog, int which) {
	        		   dialog.cancel();
					
	        	   }
	           });
	           dialog.show();	           
	           return true;

            case R.id.exitId:
                finish();
	        
	        default:
	            return super.onOptionsItemSelected(item);
	        }
	    }

    /*
    *
    * To create a new activity to launch a new note
    */
	private void createNote() {
		Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);    	
    }

    //Overriding onListItemClick
    @Override
    protected void onListItemClick(ListView l, View v, int position,final long id) {
        super.onListItemClick(l, v, position, id);
       final Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(),v);
        getMenuInflater().inflate(R.menu.note_popup,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.editItem)
                {
                    startActivityForResult(i, ACTIVITY_EDIT);
                }
                else
                if(menuItem.getItemId()==R.id.deleteItem)
                {
                    mDbHelper.deleteNote(id);
                    fillData();
                }
                return true;
            }
        });
        popupMenu.show();
        //startActivityForResult(i, ACTIVITY_EDIT);
    }

    /*
    * To load the data in the list view on loading the activity
    */
	private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);
        

        String[] from = new String[] { NotesDbAdapter.KEY_TITLE };
        int[] to = new int[] { R.id.text1};
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
            new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        setListAdapter(notes);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
       // menu.add(0, R.id.exitId, 0, R.string.exit);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.exitId:
               /* AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();*/
                finish();
               // return true;
        }
        return super.onContextItemSelected(item);
    }

    /*
    *
    * Overriding onActivityResult, on returning  back to this activity,the data will be set back again to the list view
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();        
    }   
    
}
