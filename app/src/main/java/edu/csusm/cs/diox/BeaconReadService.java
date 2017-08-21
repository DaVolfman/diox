package edu.csusm.cs.diox;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.os.SystemClock;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BeaconReadService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_TAKE_READING = "edu.csusm.cs.diox.action.READ_BEACON";

    public static final String EXTRA_RESULT = "edu.csusm.cs.diox.extra.RESULT";

    public static final String RESULT_NEW_READING = "edu.csusm.cs.diox.extra.READING_UPDATED";
    public static final String RESULT_READING = "edu.csusm.cs.diox.extra.UPDATED_READING";


    public BeaconReadService() {
        super("BeaconReadService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionReading(Context context, String param1, String param2) {
        Intent intent = new Intent(context, BeaconReadService.class);
        intent.setAction(ACTION_TAKE_READING);
        context.startService(intent);
    }

    public static Intent newRespondingIntent(Context context, ResultReceiver reciever){
        Intent rIntent = newIntent(context);
        rIntent.putExtra(EXTRA_RESULT,(Parcelable)reciever);
        return rIntent;
    }

    public static Intent newIntent(Context context){
        Intent rIntent = new Intent(context, BeaconReadService.class);
        rIntent.setAction(ACTION_TAKE_READING);
        return rIntent;
    }

    public static void startRepeatingResponding(Context context, int millis, ResultReceiver resultReceiver){
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, newRespondingIntent(context,resultReceiver), 0);
        ((AlarmManager)context.getSystemService(ALARM_SERVICE))
                .setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), millis, pendingIntent);
    }

    public static void startRepeating(Context context, int millis){
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, newIntent(context), 0);
        ((AlarmManager)context.getSystemService(ALARM_SERVICE))
                .setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), millis, pendingIntent);
    }

    public static void stopRepeating(Context context){
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, newIntent(context), 0);
        ((AlarmManager)context.getSystemService(ALARM_SERVICE))
                .cancel(pendingIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TAKE_READING.equals(action)) {
                takeSensorReading();
            }
        }
    }

    private void takeSensorReading(){
        //do the thing!
    }
}
