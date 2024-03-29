/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package net.sereko.skhomework.app.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Utility class for memoryCache
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class CacheUtil {

    /**
     * Memory Cache
     */
    protected LruCache<String, Bitmap> mMemoryCache;

    //Singleton
    private static CacheUtil sInstance;

    protected CacheUtil(){}

    public static CacheUtil getInstance() {
        if (sInstance != null)
            return sInstance;
        else
            return sInstance = new CacheUtil();
    }

    public static LruCache<String, Bitmap> getMemoryCache(){
        return CacheUtil.getInstance().mMemoryCache;
    }

    public static void putMemoryCache(LruCache<String, Bitmap> memoryCache){
        CacheUtil.getInstance().mMemoryCache=memoryCache;
    }

}
