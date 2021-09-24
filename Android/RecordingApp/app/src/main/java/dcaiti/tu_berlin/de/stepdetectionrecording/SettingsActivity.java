package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends Activity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    // TODO should we reduce redundancy here with the xml preferences resources?
    final static String PREF_AGE = "age";
    final static String PREF_HEIGHT = "height";
    final static String PREF_NAME = "name";
    final static String PREF_GENDER = "gender";

    private final static String PREF_DEFAULT_VALUE = "Unknown";

    final static String[] PREFS = {
            PREF_AGE,
            PREF_HEIGHT,
            PREF_NAME,
            PREF_GENDER
    };

    static boolean areAllSettingsValuesChanged(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (String PREF : PREFS) {
            String prefValue = prefs.getString(PREF, null);
            if (prefValue == null) {
                Log.w(TAG, "Unexpected pref value to be null.");
                return false;
            }
            if (prefValue.equals(PREF_DEFAULT_VALUE)) {
                return false;
            }
        }
        return true;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            for (String pref : PREFS) {
                bindPreferenceSummaryToValue(findPreference(pref));
            }
        }
    }
}
