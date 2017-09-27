package edu.csusm.cs.diox;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.os.SystemClock;

import java.util.Arrays;

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

    protected static final int SCAN_PERIOD = 3000;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

    private static int sHighestRSSI = 0;
    private static byte[] sHighestAdvert;
    private static Reading sReading;
    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            final byte[] iBeacon_prefix = {0x02, 0x01, 0x06, 0x1a, -1, 0x4c,0x00, 0x02, 0x15};
            final byte[] beaconatorID = {'B', 'E', 'A', 'C', 'O', 'N', 'A', 'T', 'O', 'R'};
            if(Arrays.equals(Arrays.copyOfRange(sHighestAdvert,0,9),iBeacon_prefix) &&
                    Arrays.equals(Arrays.copyOfRange(sHighestAdvert,9,19),beaconatorID) &&
                    (sHighestRSSI == 0 || sHighestRSSI < i)){
                sHighestRSSI = i;
                sHighestAdvert = bytes;
            }
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

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
                if(sReading != null){
                    intent.putExtra("Reading", sReading);
                    sReading= null;
                    sHighestRSSI= 0;
                    sHighestAdvert=null;
                }
            }
        }
    }

    private void takeSensorReading(){
        sReading = null;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mScanCallback);

                BeaconReadService.sReading = new Reading(System.currentTimeMillis() / 1000l,
                        (int)(BeaconReadService.sHighestAdvert[25]) << 16 +
                                (int)(BeaconReadService.sHighestAdvert[26]) << 8 +
                                (int)(BeaconReadService.sHighestAdvert[27]),
                        (double)(int)(BeaconReadService.sHighestAdvert[28]) * 100);
            }
        },SCAN_PERIOD);
        mBluetoothAdapter.startLeScan(mScanCallback);
    }
}
