package com.example.ivanatora.remotecommand.ExecTask;

import java.io.InputStream;

import android.os.AsyncTask;

import com.example.ivanatora.remotecommand.MainActivity;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;

import android.util.Log;


/**
 * Created by ivanatora on 06.04.17.
 */
public class ExecTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub
        Log.v("RemoteCommand1", "set comand params "+ params[0]);
        try {
            if (MainActivity.session == null) {
                return "";
            }
            InputStream in;
            Channel channel;

            channel = MainActivity.session.openChannel("exec");
            ((ChannelExec) channel).setCommand(params[0]);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                }
                if (channel.isClosed()) {
                    Log.v("RemoteCommand1", "exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    Log.v("RemoteCommand1", "exception: " + ee.toString());
                }
            }
            channel.disconnect();
            return null;
        } catch (Exception e) {
            Log.v("RemoteCommand1", "exception2: " + e.toString());
        }
        return "";
    }

}
