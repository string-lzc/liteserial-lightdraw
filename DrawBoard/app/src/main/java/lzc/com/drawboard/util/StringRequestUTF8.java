package lzc.com.drawboard.util;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;

/**
 * Created by lzc on 2017/12/17.
 */

public class StringRequestUTF8 extends StringRequest {



        public StringRequestUTF8(int method, String url,
                                 Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
// TODO Auto-generated constructor stub
        }


        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {


            String parsed = null;
            try {
                parsed = new String(response.data,
                        "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return Response.success(parsed,
                    HttpHeaderParser.parseCacheHeaders(response));


        }
    }

