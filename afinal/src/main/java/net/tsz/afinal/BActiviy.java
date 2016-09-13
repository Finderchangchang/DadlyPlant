package net.tsz.afinal;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import net.tsz.afinal.annotation.view.CodeNote;
import net.tsz.afinal.annotation.view.EventListener;
import net.tsz.afinal.annotation.view.Select;

import java.lang.reflect.Field;


/**
 * Created by Administrator on 2016/1/20.
 */
public abstract class BActiviy {
//    @Override
//    protected String getCloseWarning() {
//        return null;
//    }
//
//    @Override
//    protected int getFragmentContainerId() {
//        return 0;
//    }
//
//    public void setContentView(int layoutResID) {
//        super.setContentView(layoutResID);
//        initInjectedView(this);
//    }
//
//
//    public void setContentView(View view, ViewGroup.LayoutParams params) {
//        super.setContentView(view, params);
//        initInjectedView(this);
//    }
//
//
//    public void setContentView(View view) {
//        super.setContentView(view);
//        initInjectedView(this);
//    }
//
//
//    public static void initInjectedView(Activity activity) {
//        initInjectedView(activity, activity.getWindow().getDecorView());
//    }
//
//
//    public static void initInjectedView(Object injectedSource, View sourceView) {
//        Field[] fields = injectedSource.getClass().getDeclaredFields();
//        if (fields != null && fields.length > 0) {
//            for (Field field : fields) {
//                try {
//                    field.setAccessible(true);
//
//                    if (field.get(injectedSource) != null)
//                        continue;
//
//                    CodeNote viewInject = field.getAnnotation(CodeNote.class);
//                    if (viewInject != null) {
//
//                        int viewId = viewInject.id();
//                        field.set(injectedSource, sourceView.findViewById(viewId));
//
//                        setListener(injectedSource, field, viewInject.click(), Method.Click);
//                        setListener(injectedSource, field, viewInject.longClick(), Method.LongClick);
//                        setListener(injectedSource, field, viewInject.itemClick(), Method.ItemClick);
//                        setListener(injectedSource, field, viewInject.itemLongClick(), Method.itemLongClick);
//
//                        Select select = viewInject.select();
//                        if (!TextUtils.isEmpty(select.selected())) {
//                            setViewSelectListener(injectedSource, field, select.selected(), select.noSelected());
//                        }
//
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//
//    private static void setViewSelectListener(Object injectedSource, Field field, String select, String noSelect) throws Exception {
//        Object obj = field.get(injectedSource);
//        if (obj instanceof View) {
//            ((AbsListView) obj).setOnItemSelectedListener(new EventListener(injectedSource).select(select).noSelect(noSelect));
//        }
//    }
//
//
//    private static void setListener(Object injectedSource, Field field, String methodName, Method method) throws Exception {
//        if (methodName == null || methodName.trim().length() == 0)
//            return;
//
//        Object obj = field.get(injectedSource);
//
//        switch (method) {
//            case Click:
//                if (obj instanceof View) {
//                    ((View) obj).setOnClickListener(new EventListener(injectedSource).click(methodName));
//                }
//                break;
//            case ItemClick:
//                if (obj instanceof AbsListView) {
//                    ((AbsListView) obj).setOnItemClickListener(new EventListener(injectedSource).itemClick(methodName));
//                }
//                break;
//            case LongClick:
//                if (obj instanceof View) {
//                    ((View) obj).setOnLongClickListener(new EventListener(injectedSource).longClick(methodName));
//                }
//                break;
//            case itemLongClick:
//                if (obj instanceof AbsListView) {
//                    ((AbsListView) obj).setOnItemLongClickListener(new EventListener(injectedSource).itemLongClick(methodName));
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
//    public enum Method {
//        Click, LongClick, ItemClick, itemLongClick
//    }
}
