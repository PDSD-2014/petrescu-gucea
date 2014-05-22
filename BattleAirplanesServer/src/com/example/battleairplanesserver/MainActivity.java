package com.example.battleairplanesserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Socket clientSocket;
	private TextView textView;
	private Button startGameButton;
	private ServerSocket in;
	
	private class WaitForClient extends AsyncTask<Integer, Void, Void> {
		
		private final static String TAG = "ServerThread";
		
		@Override
		protected Void doInBackground(Integer... params) {
			Log.e(TAG, "doInBackground");
			if (params[0] == 0) {
				try {
					in = new ServerSocket(9000);
				} catch (IOException e) {
					Log.e(TAG, "Cannnot create socket. Due to: " + e.getMessage());
				}
			}
			
			try {
				clientSocket = in.accept();
					Log.d(TAG, "New request from: " + clientSocket.getInetAddress());
					SocketConnection.setSocket(clientSocket);
					publishProgress((Void)null);
			} catch (Exception e) {
				Log.e(TAG, "Error when accepting connection");
				e.printStackTrace();
			}
			
			return null;
		}
		
		 protected void onProgressUpdate(Void... progress) {
	         textView.setText("New connection: " + clientSocket.getInetAddress().toString().substring(1) + ":" + 
	        		 			clientSocket.getPort());
	         startGameButton.setVisibility(View.VISIBLE);
	     }
	}
	
	/** Called when the user clicks the Start Game button */
	public void sendMessage(View view) {
	    // Do something in response to button
		Log.i("button", "Start Game");
		if (clientSocket != null) {
			
			try {
				PrintStream writer = new PrintStream(clientSocket.getOutputStream());
				writer.println("start");
				
			} catch (IOException e) {
				Log.e("writing to socket", "error while sending start command");
				return;
			}
			Intent intent = new Intent(this, GameActivity.class);
			intent.putExtra("newGame", true);
			startActivity(intent);
		}
		else {
			// TODO: client disconected
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
		
		startGameButton = (Button)findViewById(R.id.ButStartGame);
		startGameButton.setVisibility(View.INVISIBLE);
		textView = (TextView)findViewById(R.id.MessageToEntertext);
		if (clientSocket != null)  {
			try {
				clientSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (in != null)  {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		new WaitForClient().execute(0, null, null);
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
	
	public static class SocketConnection {
		private static Socket serverSocket;
		
		public static Socket getSocket() {
			return serverSocket;
		}
		
		public static void setSocket(Socket socket) {
			serverSocket = socket;
		}
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

}
