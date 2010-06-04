package org.droidraw;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DroiDraw extends Activity implements BrushDialog.OnBrushChangedListener {
	static final int DIALOG_BRUSH_ID = 0;
	static final int BRUSH_MENU_ID = Menu.FIRST;
	static final int CLEAR_MENU_ID = Menu.FIRST + 1;
	static final int SEND_MENU_ID = Menu.FIRST + 2;
	private DrawView view;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new DrawView(this);
		setContentView(view);
	}

	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_BRUSH_ID:
			Log.v("DroiDraw", "creating dialog");
			return new BrushDialog(this, this);
		default:
			return null;
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, BRUSH_MENU_ID, 0, "Change Brush");
		menu.add(0, CLEAR_MENU_ID, 0, "Clear Canvas");
		menu.add(0, SEND_MENU_ID, 0, "Send Image");
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case BRUSH_MENU_ID:
			Log.v("DroiDraw", "showing dialog");
			showDialog(DIALOG_BRUSH_ID);
			return true;
		case CLEAR_MENU_ID:
			view.clear();
			return true;
		case SEND_MENU_ID:
			try {
				ContentValues values = new ContentValues(3);
				values.put(Media.DISPLAY_NAME, "DroiDraw painting");
				values.put(Media.DESCRIPTION, "Painted in DroiDraw");
				values.put(Media.MIME_TYPE, "image/jpeg");
				Uri image_uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
				OutputStream ostream = getContentResolver().openOutputStream(image_uri);
				view.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, ostream);
	
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/jpeg");
				intent.putExtra(Intent.EXTRA_STREAM, image_uri) ;
				startActivity(Intent.createChooser(intent, "Send Image To:"));
			} catch (FileNotFoundException e) {
				Log.v("DroiDraw", "Failed to send file:\n" + e.getMessage());
				Toast.makeText(this, "Failed to send file" , Toast.LENGTH_LONG).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void brushChanged(int c, int size) {
		view.setBrush(c, size);
	}
}
