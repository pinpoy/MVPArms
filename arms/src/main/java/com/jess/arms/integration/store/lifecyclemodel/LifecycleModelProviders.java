/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jess.arms.integration.store.lifecyclemodel;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * {@link LifecycleModelProviders} 配合 {@link LifecycleModel} 可以帮助 {@link Activity} 和 {@link Fragment}
 * 暂存和管理一些与 UI 相关以及他们必需的数据, 并且这些数据在屏幕旋转或配置更改引起的 {@link Activity} 重建的情况下也会被保留, 直到最后被 finish
 * 但是 {@link LifecycleModel} 的实现类切勿直接引用 {@link Activity} 和 {@link Fragment} 以及他们里面 UI 元素
 * <p>
 * 他还可以让开发者能够在绑定在同一个 {@link Activity} 之下的多个不同的 {@link Fragment} 之间共享数据以及通讯
 * 使用这种方式进行通讯, {@link Fragment} 之间不存在任何耦合关系
 * <p>
 * <pre>
 * public class UserLifecycleModel implements LifecycleModel {
 *
 *     public UserLifecycleModel() {
 *         // trigger user load.
 *     }
 *
 *     void doAction() {
 *         // depending on the action, do necessary business logic calls and update the
 *         // userLiveData.
 *     }
 * }
 *
 *
 * public class MyActivity extends AppCompatActivity {
 *
 *     protected void onCreate(@Nullable Bundle savedInstanceState) {
 *          LifecycleModelProviders.of(this).put(UserLifecycleModel.class.getName(), new UserLifecycleModel());
 *          fragmentManager.beginTransaction().add(R.layout.afragment_container_Id, new AFragment).commit();
 *          fragmentManager.beginTransaction().add(R.layout.bfragment_container_Id, new BFragment).commit();
 *     }
 * }
 * </pre>
 * <p>
 * 只要 AFragment 和 BFragment 绑定在同一个 Activity 下, 并使用同一个 key, 那获取到的 UserLifecycleModel 就是同一个对象
 * 这时就可以使用这个 UserLifecycleModel 进行通讯 (Fragment 之间如何通讯? 比如说接口回调? 观察者模式?) 和共享数据
 * 这时 Fragment 之间并不知道彼此, 也不互相持有, 所以也不存在耦合关系
 * <p>
 * <pre>
 * public class AFragment extends Fragment {
 *     public void onStart() {
 *         UserLifecycleModel userLifecycleModel = LifecycleModelProviders.of(getActivity()).get(UserLifecycleModel.class.getName());
 *     }
 * }
 *
 * public class BFragment extends Fragment {
 *     public void onStart() {
 *         UserLifecycleModel userLifecycleModel = LifecycleModelProviders.of(getActivity()).get(UserLifecycleModel.class.getName());
 *     }
 * }
 *
 * </pre>
 *
 * @see <a href="https://developer.android.google.cn/topic/libraries/architecture/viewmodel.html">
 * 功能和 Android Architecture 中的 ViewModel 一致, 但放开了权限不仅可以存储 ViewModel, 还可以存储任意自定义对象</a>
 * <p>
 * Created by JessYan on 21/11/2017 16:57
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 */
public class LifecycleModelProviders {


    private static Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request LifecycleModel before onCreate call.");
        }
        return application;
    }

    private static Activity checkActivity(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new IllegalStateException("Can't create LifecycleModelStore for detached fragment");
        }
        return activity;
    }

    /**
     * 返回的 {@link LifecycleModelStore} 可以在传入的这个 {code fragment} 的生命范围内一直存活者
     * {@link LifecycleModelStore} 可以存储不同的 {@link LifecycleModel} 实现类
     * 更多信息请查看 {@link LifecycleModelProviders} 顶部的注释
     *
     * @param fragment {@link LifecycleModel} 在这个 {code fragment} 的生命范围内被保留
     * @return a LifecycleModelStore instance
     */
    @MainThread
    public static LifecycleModelStore of(@NonNull Fragment fragment) {
        return LifecycleModelStores.of(fragment, checkApplication(checkActivity(fragment)));
    }

    /**
     * 返回的 {@link LifecycleModelStore} 可以在传入的这个 {code activity} 的生命范围内一直存活着
     * {@link LifecycleModelStore} 可以存储不同的 {@link LifecycleModel} 实现类
     * 更多信息请查看 {@link LifecycleModelProviders} 顶部的注释
     *
     * @param activity {@link LifecycleModel} 在这个 {code activity} 的生命范围内被保留
     * @return a LifecycleModelStore instance
     */
    @MainThread
    public static LifecycleModelStore of(@NonNull FragmentActivity activity) {
        return LifecycleModelStores.of(activity, checkApplication(activity));
    }

}
