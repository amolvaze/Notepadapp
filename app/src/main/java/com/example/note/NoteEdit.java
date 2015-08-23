/**
 * @author Kolla Satya Sai Charan
 *
 */
package com.example.note;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


import java.util.ArrayList;

/*
*
* @author Kolla Satya Sai Charan
*
 */
public class NoteEdit extends Activity{
	
	public static int numTitle = 1;	
	public static String curDate = "";
	public static String curText = "";
    public static String curTitle = "";
    private EditText mTitleText;
    private EditText mBodyText;
    private  final String defaultTitleText = "Untitled";

    private Long mRowId;

    private Cursor note;

    private NotesDbAdapter mDbHelper;
      
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
         setContentView(R.layout.note_edit);
        setTitle(R.string.app_name);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                                    : null;
        }

        populateFields();
    }

    //Static inner class to draw lines in the note pad editor
	  public static class LineEditText extends EditText{
			// we need this constructor for LayoutInflater
			public LineEditText(Context context, AttributeSet attrs) {
				super(context, attrs);
					mRect = new Rect();
			        mPaint = new Paint();
			        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			        mPaint.setColor(Color.BLUE);
			}

			private Rect mRect;
		    private Paint mPaint;	    
		    
		    @Override
		    protected void onDraw(Canvas canvas) {
		  
		        int height = getHeight();
		        int line_height = getLineHeight();

		        int count = height / line_height;

		        if (getLineCount() > count)
		            count = getLineCount();

		        Rect r = mRect;
		        Paint paint = mPaint;
		        int baseline = getLineBounds(0, r);

		        for (int i = 0; i < count; i++) {

		            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
		            baseline += getLineHeight();

		        super.onDraw(canvas);
		    }

		}
	  }
	  
	  @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        //saveState();
	        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
	    }
	    
	    @Override
	    protected void onPause() {
	        super.onPause();
	        //saveState();
	    }
	    
	    @Override
	    protected void onResume() {
	        super.onResume();
	        populateFields();
	    }
	    
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.noteedit_menu, menu);
			//MenuItem item = menu.add(0, R.id.menu_cancel, 1, R.string.cancel_menu_noteedit);
			//item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			
			return super.onCreateOptionsMenu(menu);	
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
		    switch (item.getItemId()) {
		    case R.id.menu_about:
		          
		    	/* Here is the introduce about myself */	    	
		        AlertDialog.Builder dialog = new AlertDialog.Builder(NoteEdit.this);
		        dialog.setTitle("About");
		        dialog.setMessage("Demo for assignment3,UTD");
		        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
		        	   @Override
		        	   public void onClick(DialogInterface dialog, int which) {
		        		   dialog.cancel();
						
		        	   }
		           });
		           dialog.show();	           
		           return true;
		    /*case R.id.menu_delete:
				if(note != null){
	    			note.close();
	    			note = null;
	    		}
	    		if(mRowId != null){
	    			mDbHelper.deleteNote(mRowId);
	    		}
	    		finish();
		    	
		        return true;*/
		    case R.id.menu_save:

                String enteredTitle = mTitleText.getText().toString();
                if(enteredTitle.equalsIgnoreCase("")) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(NoteEdit.this);
                    alertDialog.setTitle("Notepad");
                    alertDialog.setMessage("Do you want to save changes as Untitled?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mTitleText.setText("Untitled");
                            saveData(defaultTitleText,"save");
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    alertDialog.show();

                }
                else
                {
                    saveData(enteredTitle,"save");
                }


                return true;
	    	
		    case R.id.menu_cancel:

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(NoteEdit.this);
                alertDialog.setTitle("Notepad");
                alertDialog.setMessage("Do you want to save changes?");
                alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveState("save");
                        finish();
                    }
                });
                alertDialog.setNegativeButton("Don't Save",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alertDialog.show();
                return true;
		    	//finish();

             case R.id.saveAsItemId:
                 saveAsData();


		    default:
		    	return super.onOptionsItemSelected(item);
		    }
		}

        /*
        *
        * To make DB call,for saving data into the database.
        * It creates a new entry if id is null and updates the entry if id is null for save activity
        * It creates a new instance for save as functionality
         */
	    private void saveState(String state) {//Save or SaveAs State
	        String title = mTitleText.getText().toString();
	        String body = mBodyText.getText().toString();

            if (state.equalsIgnoreCase("save")) {
                if (mRowId == null) {
                    long id = mDbHelper.createNote(title, body);
                    if (id > 0) {
                        mRowId = id;
                    } else {
                        Log.e("saveState", "failed to create note");
                    }
                } else {
                    if (!mDbHelper.updateNote(mRowId, title, body)) {
                        Log.e("saveState", "failed to update note");
                    }
                }
            }
            else if(state.equalsIgnoreCase("saveAs"))
            {
                long id = mDbHelper.createNote(title, body);
                if(id > 0){
                    mRowId = id;
                }else{
                    Log.e("saveState","failed to create note");
                }
            }
        }

    /*
    *
    * Method to invoke save call on clicking save item
     */
    public void saveData(String enteredTitle,String state) {

        if (curTitle.equalsIgnoreCase(enteredTitle) ) {
            saveState(state);
            finish();
        } else {
            boolean isDuplicate = isDuplicate(enteredTitle);
            if (!isDuplicate) {
                saveState(state);
                finish();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(NoteEdit.this);
                alertDialog.setTitle("Notepad");
                alertDialog.setMessage("File Name already Exists, Please choose a new file name?");
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                alertDialog.show();

            }
        }
    }

    /*
    *
    * To implement save as functionality on clicking save as item
     */
    public void saveAsData()
    {
        String enteredTitle = mTitleText.getText().toString();
        if(enteredTitle.equalsIgnoreCase("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(NoteEdit.this);
            alertDialog.setTitle("Notepad");
            alertDialog.setMessage("Do you want to save changes as Untitled?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mTitleText.setText("Untitled");
                    saveData(defaultTitleText,"saveAs");
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alertDialog.show();

        }
        else {
            boolean isDuplicate = isDuplicate(enteredTitle);
            if (!isDuplicate) {
                saveState("saveAs");
                finish();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(NoteEdit.this);
                alertDialog.setTitle("Notepad");
                alertDialog.setMessage("File Name already Exists, Please choose a new file name?");
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                alertDialog.show();

            }
        }
    }


    /*
    *
    * To display the values in the text editor
    * It mainly plays role in edit phase
     */
    private void populateFields() {
	        if (mRowId != null) {
	            note = mDbHelper.fetchNote(mRowId);
	            startManagingCursor(note);
	            mTitleText.setText(note.getString(
	    	            note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
	            mBodyText.setText(note.getString(
	                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
	            curText = note.getString(
	                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
                curTitle = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
	        }
	    }

    /*
    *
    * It returns the titles for all the notepads present in DB.
     */
    private ArrayList<String> getNotepadTitles()
    {
        ArrayList<String> notepadList = new ArrayList<String>();
        Cursor tableData = mDbHelper.fetchAllNotes();
        startManagingCursor(tableData);
        if (tableData.moveToFirst()){
            do{
                notepadList.add(tableData.getString(tableData.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
                // do what ever you want here
            }while(tableData.moveToNext());
        }
        tableData.close();
        return notepadList;
    }

    /*
    *
    * Validating for duplicate file name, It verifies if the file name is already captured
     */
    public boolean isDuplicate(String title)
    {
        ArrayList<String> titleList = getNotepadTitles();
        //boolean isExist =  titleList.contains(title);
        return titleList.contains(title);
    }




}
