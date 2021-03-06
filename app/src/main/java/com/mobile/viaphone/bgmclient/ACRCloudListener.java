package com.mobile.viaphone.bgmclient;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ACRCloudListener implements IACRCloudListener {

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;
    private Config config;

    private boolean mProcessing = false;
    private boolean initState = false;

    private Context context;
    private TextView mVolume, mResult;


    public ACRCloudListener(Config config, Context context, TextView mVolume, TextView mResult) {
        this.config = config;
        this.context = context;
        this.mVolume = mVolume;
        this.mResult = mResult;
    }

    public void start() {
        if (!this.initState) {
            AcrConfig acrConfig = HttpHelper.getAcrConfig();
            this.mConfig = new ACRCloudConfig();
            this.mConfig.acrcloudListener = this;
            this.mConfig.context = context;
            this.mConfig.host = acrConfig.getHost();
            this.mConfig.accessKey = acrConfig.getAccessKey();
            this.mConfig.accessSecret = acrConfig.getAccessSecret();
            this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;

            this.mClient = new ACRCloudClient();
            this.initState = this.mClient.initWithConfig(this.mConfig);
            if (!this.initState) {
                Toast.makeText(context, "init error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!mProcessing) {
            mProcessing = true;
            mVolume.setText("");
            mResult.setText("");
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false;
                mResult.setText("start error!");
            }
        }
    }

    protected void stopRecordToRecognize() {
        if (mProcessing && this.mClient != null) {
            this.mClient.stopRecordToRecognize();
        }
        mProcessing = false;
    }

    protected void cancel() {
        if (mProcessing && this.mClient != null) {
            mProcessing = false;
            this.mClient.cancel();
//            tv_time.setText("");
            mResult.setText("");
        }
    }

    public void destroy() {
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }
    }

    @Override
    public void onResult(String result) {
        if (this.mClient != null) {
            this.mClient.cancel();
            mProcessing = false;
        }
        mResult.setText(result);

        MusicRec music = parseAcr(result);
        if (music != null) {
            HttpHelper.sendMusic(music, config.getAuth());
        }
    }

    @Override
    public void onVolumeChanged(double volume) {
        mVolume.setText(String.valueOf(volume));
    }

    private MusicRec parseAcr(String result) {

        try {
            JSONObject jResult = new JSONObject(result);
            JSONObject status = jResult.getJSONObject("status");
            if (status.getInt("code") == 0) {
                JSONObject metadata = jResult.getJSONObject("metadata");
                if (metadata.has("music")) {

                    MusicRec musicReq = new MusicRec();
                    musicReq.setCustomer("islam");

                    JSONArray musics = metadata.getJSONArray("music");
                    JSONObject tt = (JSONObject) musics.get(0);
                    Music music = new Music();
                    music.setAcrid(tt.getString("acrid"));

                    if (!tt.isNull("title")) {
                        music.setTitle(tt.getString("title"));
                    }

                    if (!tt.isNull("label")) {
                        music.setLabel(tt.getString("label"));
                    }

                    if (!tt.isNull("duration_ms")) {
                        music.setDuration(tt.getLong("duration_ms") / 1000);
                    }

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    if (!tt.isNull("release_date")) {
                        music.setReleaseDate(format.parse(tt.getString("release_date")));
                    }

                    if (!tt.isNull("album")) {
                        music.setAlbum(tt.getJSONObject("album").getString("name"));
                    }

                    if (!tt.isNull("genres")) {
                        JSONArray genres = tt.getJSONArray("genres");
                        for (int j = 0; j < genres.length(); j++) {
                            JSONObject genre = (JSONObject) genres.get(j);
                            music.addGenre(genre.getString("name"));
                        }
                    }

                    if (!tt.isNull("artists")) {
                        JSONArray artists = tt.getJSONArray("artists");
                        for (int j = 0; j < artists.length(); j++) {
                            JSONObject artist = (JSONObject) artists.get(j);
                            music.addArtist(artist.getString("name"));
                        }
                    }

                    if (!tt.isNull("play_offset_ms")) {
                        music.setPlayOffset(tt.getLong("play_offset_ms"));
                    }

                    if (!tt.isNull("external_ids")) {
                        JSONObject externalIds = tt.getJSONObject("external_ids");
                        if (!externalIds.isNull("isrc")) {
                            music.setIsrcCode(externalIds.getString("isrc"));
                        }

                        if (!externalIds.isNull("upc")) {
                            music.setUpcCode(externalIds.getString("upc"));
                        }
                    }

                    musicReq.setMusic(music);
                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    musicReq.setRecDate(format.parse(metadata.getString("timestamp_utc")));
                    return musicReq;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
