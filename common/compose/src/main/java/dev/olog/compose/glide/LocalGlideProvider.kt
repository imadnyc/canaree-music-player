/**
 * Designed and developed by 2020 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.olog.compose.glide

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.skydoves.landscapist.glide.LocalGlideRequestBuilder
import com.skydoves.landscapist.glide.LocalGlideRequestManager

internal object LocalGlideProvider {

    @Composable
    fun getGlideRequestBuilder(imageModel: Any?): RequestBuilder<Drawable> {
        return LocalGlideRequestBuilder.current
            ?: getGlideRequestManager()
                .asDrawable()
                .load(imageModel)
    }

    @Composable
    fun getGlideRequestManager(): RequestManager {
        // By default Glide tries to install lifecycle listeners to automatically re-trigger
        // requests when resumed. We don't want that with Compose, since we rely on composition
        // for our 'lifecycle'. We can stop Glide doing this by using the application context.
        return LocalGlideRequestManager.current
            ?: Glide.with(LocalContext.current.applicationContext)
    }
}
