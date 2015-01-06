
package com.cooeeui.brand.zenlauncher.unittest.category;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class CateBloom extends CateBloomJni {
    static CateBloom _instance = null;
    private long blocks = 0;
    Context mContext;

    private CateBloom(Context context) {
        super();
        mContext = context;
    }

    public static void init(Context context) {
        if (_instance == null) {
            _instance = new CateBloom(context);
            if (!_instance.init()) {
                _instance = null;
            }
        }
    }

    public static void close() {
        if (_instance != null) {
            _instance.finalize();
            _instance = null;
        }
    }

    public static int pnameToCaid(String pname) {
        if (0 == _instance.blocks) {
            return -1;
        }

        int caid = -1;
        if (0 <= (caid = _instance.pname2caid(_instance.blocks, pname))) {
            return caid;
        }
        return -1;
    }

    protected void finalize() {
        // Think: why the finalize be called 2 times sometimes.
        // Notice: the guard is necessary.
        if (blocks != 0) {
            this.free(blocks);
            blocks = 0;
        }
    }

    protected Boolean initByPath(String block_path) {
        long blocks = 0;
        if (0 < (blocks = this.initpath(block_path))) {
            this.blocks = blocks;
            return true;
        }
        return false;
    }

    private Boolean init() {
        return this.initByAssetPath("cate_bloom_" + version + ".bin");
    }

    private Boolean initByAssetPath(String asset_path) {
        InputStream inStream = null;
        try {
            inStream = mContext.getAssets().open(asset_path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (!this.initByBuffer(swapStream.toByteArray(), swapStream.size())) {
            return false;
        }
        return true;
    }

    private Boolean initByBuffer(byte[] buffer, int len) {
        long blocks = 0;
        if (0 < (blocks = this.initbytes(buffer, len))) {
            this.blocks = blocks;
            return true;
        }
        return false;
    }
}
