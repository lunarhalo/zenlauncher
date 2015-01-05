/**
 * @file CateBloomJni.java
 * @brief 
 * @author TangJunxing, 385749807@qq.com
 * @version 0.1alpha
 * @date 2014-12-23
 */

package com.cooeeui.brand.zenlauncher.unittest.category;

public class CateBloomJni
{
    protected native long initpath(String block_path);

    protected native long initbytes(byte[] buffer, int len);

    protected native int pname2caid(long blocks, String pname);

    protected native void free(long blocks);

    static String version = "v1";
    static {
        System.loadLibrary("cate_bloom_jni_" + version);
    }
}
