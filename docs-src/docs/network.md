## Introduction
Developers can use the **Easy Network** module to get instant updates about the change in network state in `Activiy` or `Fragment`. `Activity` or `Fragment` can implement `NetworkChangeTrackerListener` and create instance of the `NetworkChangeTracker` to make it possible to get the instantanious update about the network change.

Call `NetworkChangeTracker.startTracking(Context)` to start the network status tracking and `NetworkChangeTracker.stopTracking(Context)` when the tracking no more needed.

### Eaxample
Example below will show how to use `NetworkChangeTracker` in an `Activity`.

    public class MainActivity extends AppCompatActivity implements NetworkChangeTrackerListener {

        NetworkChangeTracker tracker;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            tracker = NetworkChangeTracker(this)
        }

        @Override
        public void onStart(){
            tracker.startTracking(MainActivity.this);
        }


        @Override
        public void onPasue(){
            tracker.stopTracking(MainActivity.this);
        }

        @Override
        void onNetworkChange(NetworkInfoGroup networkInfoGroup){

        }
    }

