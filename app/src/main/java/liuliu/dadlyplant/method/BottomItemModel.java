package liuliu.dadlyplant.method;

/**
 * Created by Administrator on 2016/10/7.
 */
public class BottomItemModel {
    private String val;//底部文字
    private int normal_img;//正常的图片
    private int pressed_img;//点击的图片

    public BottomItemModel(String val, int normal_img, int pressed_img) {
        this.val = val;
        this.normal_img = normal_img;
        this.pressed_img = pressed_img;
    }

    public BottomItemModel(String val, int normal_img) {
        this.val = val;
        this.normal_img = normal_img;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public int getNormal_img() {
        return normal_img;
    }

    public void setNormal_img(int normal_img) {
        this.normal_img = normal_img;
    }

    public int getPressed_img() {
        return pressed_img;
    }

    public void setPressed_img(int pressed_img) {
        this.pressed_img = pressed_img;
    }
}
