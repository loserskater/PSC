package com.pokescanner.objects;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokescanner.settings.Settings;
import com.pokescanner.utils.DrawableUtils;
import com.pokescanner.utils.UiUtils;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PokeStop extends RealmObject
{
    double latitude, longitude;
    @PrimaryKey
    String id;
    @Index
    String activePokemonName;
    boolean hasLureInfo;
    long lureExpiryTimestamp;
    long activePokemonNo;
    public PokeStop()
    {
    }

    public PokeStop(Pokestop pokestopData)
    {
        setLatitude(pokestopData.getLatitude());
        setLongitude(pokestopData.getLongitude());
        setId(pokestopData.getId());
        setHasLureInfo(pokestopData.getFortData().hasLureInfo());
        setLureExpiryTimestamp(pokestopData.getFortData().getLureInfo().getLureExpiresTimestampMs());
        setActivePokemonNo(pokestopData.getFortData().getLureInfo().getActivePokemonId().getNumber());
        setActivePokemonName(pokestopData.getFortData().getLureInfo().getActivePokemonId().toString());
    }

    public DateTime getExpiryTime()
    {
        return new DateTime(getLureExpiryTimestamp());
    }

    public MarkerOptions getMarker(Context context)
    {
        String uri = "";
        String snippetMessage  = "";
        String iconMessage = "Pokestop";
        if(hasLureInfo) //There is a lure active at the pokestop
        {
            if(getExpiryTime().isAfter(new Instant())){
                String activePokemonName = getActivePokemonName();
                activePokemonName = activePokemonName.substring(0, 1).toUpperCase() + activePokemonName.substring(1).toLowerCase();
                snippetMessage = "A lure is active here, and has attracted a " + activePokemonName;
            }
        }

        LatLng position = new LatLng(getLatitude(), getLongitude());

        MarkerOptions pokestopMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(context)))
                .position(position);
        if (Settings.getPreferenceBoolean(context, Settings.KEY_OLD_MARKER))
        {
            pokestopMarker.title("Pokestop");
            pokestopMarker.snippet(snippetMessage);
        }
        return pokestopMarker;
    }

    public String getLuredPokemonName()
    {
        String luredPokemonName = getActivePokemonName();
        luredPokemonName = luredPokemonName.substring(0, 1).toUpperCase() + luredPokemonName.substring(1).toLowerCase();
        return luredPokemonName;
    }


    public String getExpireTime() {
        return DrawableUtils.getExpireTime(getLureExpiryTimestamp());
    }

    public Bitmap getBitmap(Context context)
    {
        int pokeStopType = DrawableUtils.PokeStopType;
        int pokemonnumber = (int) getActivePokemonNo();

        String uri = "stop";

        if(hasLureInfo && getExpiryTime().isAfter(new Instant())) {
            uri = "stop_lure";
            pokeStopType = DrawableUtils.LuredPokeStopType;
            //if ShowLuredPokemon is enabled, show the icon of the lured pokemon
            if (Settings.getPreferenceBoolean(context, Settings.SHOW_LURED_POKEMON)) {
                if (Settings.getPreferenceBoolean(context, Settings.SHUFFLE_ICONS)) {
                    uri = "ps" + pokemonnumber;
                }
                else
                {
                    uri = "p" + pokemonnumber;
                }
            }

            //but don't show it if it's filtered, just show the lured pokestop icon
            if (UiUtils.isPokemonFiltered(pokemonnumber)) {
                uri = "stop_lure";
            }
        }

        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());
        Bitmap out = DrawableUtils.getBitmapFromView(resourceID, DrawableUtils.getExpireTime(getLureExpiryTimestamp()), context, pokeStopType);

        return out;
    }
}
