package edu.csusm.cs.diox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient mLocationProvider;
    private Reading mLastReading;

    static final int CURRENT_READING_REQUEST = 1;

    private TextView outputLine = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputLine = (TextView) findViewById(R.id.reading_output);

        mLocationProvider = LocationServices.getFusedLocationProviderClient(this);
    }

    private void updateLocation(){
        Intent intent = BeaconReadService.newRespondingIntent(this,new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int rcode, Bundle rdata){
                if(rdata.getBoolean(BeaconReadService.RESULT_NEW_READING, false) == true){
                    mLastReading = (Reading) rdata.getParcelable(BeaconReadService.RESULT_READING);
                    updateReadingLine();
                }
            }
        });
        startService(intent);
    }

    private void updateReadingLine(){
        StringBuilder formatter = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

        if(null == mLastReading){
            outputLine.setText("none");
        }else{
            formatter.append(dateFormat.format(new Date(mLastReading.getTimeEpochSeconds() * 1000L)));
            formatter.append(", Beacon:").append(mLastReading.getBeaconID());
            formatter.append(", ").append(mLastReading.getConcentrationPPM()).append("PPM CO2");
            outputLine.setText(formatter.toString());
        }
    }

    private void startService(){
        Intent intent = BeaconReadService.newRespondingIntent(this,new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int rcode, Bundle rdata){
                if(rdata.getBoolean(BeaconReadService.RESULT_NEW_READING, false) == true){
                    mLastReading = (Reading) rdata.getParcelable(BeaconReadService.RESULT_READING);
                    updateReadingLine();
                }
            }
        });
        PendingIntent pendingIntent = PendingIntent.getService(this,0, intent,0);

        ((AlarmManager)getSystemService(ALARM_SERVICE)).setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(),5*60*1000,pendingIntent);
    }

    private void stopService(){

    }
}

