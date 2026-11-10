/**
 * Copyright (C) 2020 Chenhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cc.chenhe.weargallery

import android.app.Application
import android.os.Build
import android.util.Log
import cc.chenhe.weargallery.common.log.MmapLogTree
import coil.ComponentRegistry
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class MyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        cc.chenhe.weargallery.ui.compose.WearThemePreferences.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(MmapLogTree(this, Log.DEBUG))
        }

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .components(fun ComponentRegistry.Builder.() {
            // https://coil-kt.github.io/coil/gifs/#gifs
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        })
        .build()

}