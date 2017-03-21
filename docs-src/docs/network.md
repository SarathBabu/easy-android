During the running of application the network may go off and reconnect at multiple times, and we may want to get the status of the network instanataniously in `Activiy` or `Fragment`. `Activity` or `Fragment` can implement `NetworkChangeTrackerListener` and create instance of the `NetworkChangeTracker` to make it possible to get the instantanious update about the network change.

Call `NetworkChangeTracker.startTracking(Context)` to start the network status tracking and `NetworkChangeTracker.stopTracking(Context)` when the tracking no more needed.

Example for enabling tracking in `Activity` is given bellow.

    public class MainActivity extends AppCompatActivity implements NetworkChangeTrackerListener{

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
