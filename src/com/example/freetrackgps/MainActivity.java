package com.example.freetrackgps;

import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.location.*;
import android.content.*;
import android.view.MenuItem;


import android.view.View;

public class MainActivity extends Activity {
	private LocationManager service;
	private TextView gpsStatus, gpsPosition, workoutStatus, workoutDistance; 
	private String provider;
	private List<TextView> textViewElements;
	private Button pauseButton, startButton;
    private SharedPreferences SP;
	private RouteManager currentRoute;

	protected void onCreate(Bundle savedInstanceState) {
        SP = getSharedPreferences("Pref", Activity.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		service = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		gpsStatus = (TextView)this.findViewById(R.id.textGPSStatus);
		gpsPosition = (TextView)this.findViewById(R.id.textPosition);
		workoutStatus = (TextView)this.findViewById(R.id.textSatlites);
		workoutDistance = (TextView)this.findViewById(R.id.textWorkOut);
		textViewElements = Arrays.asList(gpsPosition, gpsStatus, workoutStatus, workoutDistance);
		pauseButton = (Button)this.findViewById(R.id.pauseButton);
		startButton = (Button)this.findViewById(R.id.startButton);
		currentRoute = new RouteManager(this);
		startButton.setOnClickListener(new View.OnClickListener() { 
			public void onClick(View V){ onStartRoute();}
		});
		pauseButton.setOnClickListener(new View.OnClickListener() { 
			public void onClick(View V){ onPauseRoute();}
		});	

        onCreateGPSConnection(textViewElements, service, currentRoute);
        if(currentRoute.getStatus()!= 2){
            setPreviewStatus(View.INVISIBLE);
        }else{
            setPreviewStatus(View.VISIBLE);
        }
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        if (currentRoute.getStatus() != 2) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return  false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                Intent inent = new Intent(this, Settings.class);
                startActivity(inent);
                break;
        }
        return true;
    }

    public void onCreateGPSConnection(List<TextView> textViewElements, LocationManager service, RouteManager currentRoute){
        if (service != null){
            int[] time = getResources().getIntArray(R.array.arr);
            int[] distance = getResources().getIntArray(R.array.sarr);
            service.requestLocationUpdates(LocationManager.GPS_PROVIDER, time[(SP.getInt("time", 1))], distance[(SP.getInt("distance",1))], new GPSListner(textViewElements, currentRoute) );
            textViewElements.get(1).setText("On");
            Location L= (Location)service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (L != null){
                String message = String.format( " %1$s %2$s", String.format( "%.2f", L.getLongitude()), String.format( "%.2f", L.getLatitude()),  String.format( "%.2f", L.getAltitude()) );
                if (currentRoute.getStatus() !=0)
                    textViewElements.get(0).setText(message);
            }
        }
        else{
            textViewElements.get(1).setText("Off");
            int[] time = getResources().getIntArray(R.array.arr);
            int[] distance = getResources().getIntArray(R.array.sarr);
            service.requestLocationUpdates(LocationManager.GPS_PROVIDER, time[(SP.getInt("time", 1))], distance[(SP.getInt("distance",1))], new GPSListner(textViewElements, currentRoute) );
        }
    }
    public void setPreviewStatus(int status){
        if (SP.getBoolean("showWorkoutInfo", false)==false) {
            workoutDistance.setVisibility(status);
            workoutStatus.setVisibility(status);
            ((TextView) this.findViewById(R.id.textView4)).setVisibility(status);
            ((TextView) this.findViewById(R.id.textView3)).setVisibility(status);
        }
    }
	public void onStartRoute() {
		if (currentRoute.getStatus() == 0){
			currentRoute.start();
			workoutStatus.setText("Active");
			startButton.setText("Stop");
            setPreviewStatus(View.VISIBLE);
		}
		else{
			currentRoute.stop();
			workoutStatus.setText("--");
			startButton.setText("Start");
            setPreviewStatus(View.INVISIBLE);
		}	
	}
	public void onPauseRoute(){
			if(currentRoute.getStatus() == 2){
				currentRoute.pause();
				workoutStatus.setText("Pause");
				pauseButton.setText("Unpause");
			}
			else if (currentRoute.getStatus() == 1){
				currentRoute.unpause();
				workoutStatus.setText("Active");
				pauseButton.setText("Pause");
			}
	}
}