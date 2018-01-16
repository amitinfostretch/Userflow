package userbehaviour.com.infostretch.amit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by amit.siddhpura on 09-01-2018.
 */

public class Utils {
    private static final String TAG = "Utils";
    private static ArrayList<String> activityFlowArray = new ArrayList<String>();
    public static long startTime;
    private static String sessionId = "";
    public static FirebaseUser currentUser;

    public static void predictUserEvent(FirebaseAnalytics mFirebaseAnalytics, String activityName) {
        Bundle bundle = new Bundle();
        bundle.putString("Activity_Open", activityName);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public static void trackActivity(String name) {
        final String activityName = name.substring(name.indexOf(".") + 1);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Activity");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                // Get Post object and use the values to update the UI
                Integer number = dataSnapshot.getValue(Integer.class);
                if (number == null) {
                    myRef.child(activityName).setValue(1);
                } else {
                    number++;
                    myRef.child(activityName).setValue(number);
                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        };
        myRef.child(activityName).addListenerForSingleValueEvent(postListener);
    }

    public static void trackActivityTime(final String activityName, final long time) {
        final String name = activityName.substring(activityName.indexOf(".") + 1);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("ActivityTimeFlow");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Long number = dataSnapshot.getValue(Long.class);
                if (number == null) {
                    myRef.child(name).setValue(time);
                } else {
                    long t = time + number;
                    myRef.child(name).setValue(t);
                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        };
        myRef.child(activityName).addListenerForSingleValueEvent(postListener);

    }

    public static void trackTime(String uID, Activity activity, boolean isResume) {
        String name = activity.getLocalClassName();
        String activityName = name.substring(name.indexOf(".") + 1);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (isResume) {
            SharedPreferences.Editor ed = preferences.edit();
            ed.putLong(activityName, (System.currentTimeMillis() / 1000));
            ed.commit();
        } else {
            long end = (System.currentTimeMillis() / 1000);

            preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            startTime = preferences.getLong(activityName, 0);
            if (startTime != 0) {
                long elapsedTime = end - startTime;
                trackActivityTime(uID + "/" + activityName, elapsedTime);
            }


        }
    }

    public static void trackActivityFlow(final String name) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String flowOfActivity = name.substring(name.indexOf(".") + 1);
        final DatabaseReference myRef = database.getReference("ActivityFlow").child(flowOfActivity);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Integer number = dataSnapshot.getValue(Integer.class);
                if (number == null) {
                    myRef.setValue(0);
                } else {
                    number++;
                    myRef.setValue(number);

                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        };
        myRef.addListenerForSingleValueEvent(postListener);
    }

    public static void addUserBehaviour(final String name) {
        activityFlowArray.add(name);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String flowOfActivity = name.substring(name.indexOf(".") + 1);
        final DatabaseReference myRef = database.getReference("ActivityFlow").child(flowOfActivity);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Integer number = dataSnapshot.getValue(Integer.class);
                if (number == null) {
                    myRef.setValue(0);
                } else {
                    number++;
                    myRef.setValue(number);

                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        };
        myRef.addListenerForSingleValueEvent(postListener);
    }

    public static String getUserBehaviourKey() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        sessionId = database.getReference("UserBehaviour").push().getKey();
        return sessionId;
    }

    public static void addUserBehaviour(Activity activity, final String behaviourName) {
        if (sessionId.equals("")) {
            getUserBehaviourKey();
        }
        activityFlowArray.add(behaviourName);
        if (currentUser != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference("UserBehaviour");
            databaseReference.child(currentUser.getUid()).child(sessionId).setValue(activityFlowArray);
        }else{
            getCurrentUser(activity);
        }
    }

    public static void trackStartAppTime(final String time) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("StartAppTime");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = myRef.push().getKey();
                myRef.child(key).setValue(time);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        };
        myRef.addListenerForSingleValueEvent(postListener);
    }

    public static void getCurrentUser(final Activity activity) {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInAnonymously:success");
                            currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        } else {
                            task.getException().printStackTrace();
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                        }
                    }
                });
    }

}
