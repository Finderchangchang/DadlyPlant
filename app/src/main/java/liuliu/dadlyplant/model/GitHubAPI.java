package liuliu.dadlyplant.model;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Administrator on 2016/9/20.
 */

public interface GitHubAPI {
    /*
       请求该接口：https://api.github.com/users/baiiu
     */
    @GET("txapi/mvtp/meinv?num={user}")
    Observable<UserModel> userInfo(@Path("user") String user);
}
