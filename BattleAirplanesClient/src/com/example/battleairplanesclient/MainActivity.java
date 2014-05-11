package com.example.battleairplanesclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;

public class MainActivity extends Activity {
	private Socket serverSocket;
	private EditText editText;
	private TextView textView; 
	private Button startGameButton;
	
	private class ServerConnectThread extends AsyncTask<MainActivity, Integer, Void> {
		private final static String TAG = "ClientThread";
		
		@Override
		protected Void doInBackground(MainActivity... params) {
			Log.e(TAG, "doInBackground");
			
			try {
				serverSocket = new Socket(editText.getText().toString(), 9000);
				Log.e(TAG, "Connected to server");
				publishProgress(0);
				
				BufferedReader responseReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
				String line = responseReader.readLine();
				Log.i(TAG, line);
				Intent intent = new Intent(params[0], GameActivity.class);
				startActivity(intent);
				
			} catch (UnknownHostException e) {
				Log.e(TAG, "Unknow Host");
				publishProgress(1);
			} catch (IOException e) {
				Log.e(TAG, "Error opening client socket");
				publishProgress(2);
			}
			
			return null;
		}
		
		 protected void onProgressUpdate(Integer... progress) {
			if (progress[0] == 0) {
				textView.setText("Connected to: " + serverSocket.getInetAddress().toString().substring(1) + ":" 
						+ serverSocket.getLocalPort());
				editText.setVisibility(View.INVISIBLE);
				startGameButton.setVisibility(View.INVISIBLE);
			}
			else if (progress[1] == 1) 
				textView.setText("Unknown Host");
			else
				textView.setText("Error opening clientsocket");
	     }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		editText = (EditText)findViewById(R.id.TextIpFriend);
		textView = (TextView)findViewById(R.id.MessageToEntertext);
		startGameButton = (Button)findViewById(R.id.ButStartGame);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	/** Called when the user clicks the Start Game button */
	public void sendMessage(View view) {
	    // Do something in response to button
		
		new ServerConnectThread().execute(this, null, null);
	}

}
