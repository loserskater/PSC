package com.pokescanner.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.pokescanner.helper.ExpirationFilter;
import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.objects.NotificationItem;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;
import com.pokescanner.settings.Settings;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import io.realm.Realm;

import static com.pokescanner.helper.Generation.hexagonal_number;


public class UiUtils {
    public static final int BASE_DELAY = 1000;

    public static void hideKeyboard(EditText editText) {
        ((InputMethodManager) editText.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static String getSearchTimeString(int val, Context context) {
        if (Realm.getDefaultInstance().where(User.class).findAll().size() <= 0) {
            return "99:99";
        }
        int serverRefreshValue = (BASE_DELAY * Settings.getPreferenceInt(context, Settings.SERVER_REFRESH_RATE));
        int serverDividedValue = serverRefreshValue / Realm.getDefaultInstance().where(User.class).findAll().size();
        int calculatedValue = hexagonal_number(val) * serverDividedValue;
        System.out.println(serverRefreshValue + " " + serverDividedValue + " " + calculatedValue);
        long millis = calculatedValue;
        DateTime dt = new DateTime(millis);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
        return fmt.print(dt);
    }

    public static long getSearchTimeLong(int val, Context context) {
        int serverRefreshValue = (BASE_DELAY * Settings.getPreferenceInt(context, Settings.SERVER_REFRESH_RATE));
        int serverDividedValue = serverRefreshValue / Realm.getDefaultInstance().where(User.class).findAll().size();
        int calculatedValue = hexagonal_number(val) * serverDividedValue;
        System.out.println(serverRefreshValue + " " + serverDividedValue + " " + calculatedValue);
        return calculatedValue;
    }

    public static boolean isPokemonExpiredFiltered(Pokemons pokemons, Context context) {
        long millis = ExpirationFilter.getFilter(context).getPokemonExpirationMinSec() * BASE_DELAY;
        //Create a date from the expire time (Long value)
        DateTime date = new DateTime(pokemons.getExpires());
        //If our date time is after now then it's expired and we'll return expired (So we don't get an exception
        if (date.isAfter(new Instant())) {
            Interval interval;
            interval = new Interval(new Instant(), date);

            return millis > interval.toDurationMillis();
        }
        return false;
    }

    public static boolean isPokemonFiltered(Pokemons pokemons) {
        //lol this is really long but it's simple and to the point
        return PokemonListLoader.getFilteredList().contains(new FilterItem(pokemons.getNumber()));
    }

    public static boolean isPokemonFiltered(int number) {
        //lol this is really long but it's simple and to the point
        return PokemonListLoader.getFilteredList().contains(new FilterItem(number));
    }

    public static ArrayList<String> getAllProfiles(Context context) {
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        for (NotificationItem notificationItem : PokemonListLoader.getNotificationList()) {
            hashSet.addAll(notificationItem.getProfiles());
        }
        //In case the user hasn't added any pokemon to that profile yet
        hashSet.add(Settings.getPreferenceString(context, Settings.PROFILE));

        return new ArrayList<>(hashSet);
    }
}
