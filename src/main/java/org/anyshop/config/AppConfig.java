package org.anyshop.config;

import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.druid.DruidPlugin;
import cn.dreampie.route.config.*;
import cn.dreampie.route.handler.cors.CORSHandler;
import cn.dreampie.route.interceptor.security.SecurityInterceptor;
import cn.dreampie.route.interceptor.transaction.TransactionInterceptor;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;

/**
 * Created by wangrenhui on 15/1/15.
 */
public class AppConfig extends Config {
  private static final Prop prop = Proper.use("application.properties");

  public void configConstant(ConstantLoader constantLoader) {
    constantLoader.setDevMode(prop.getBoolean("devMode", false));
    constantLoader.setCacheEnable(true);
  }

  public void configResource(ResourceLoader resourceLoader) {
    //扫描的  resource 目录
    resourceLoader.addIncludePaths("org.anyshop.resource");
  }

  public void configPlugin(PluginLoader pluginLoader) {
    DruidPlugin druidPlugin = new DruidPlugin(prop.get("db.default.url"), prop.get("db.default.user"), prop.get("db.default.password"), prop.get("db.default.driver"), prop.get("db.default.dialect"));
    // StatFilter提供JDBC层的统计信息
    druidPlugin.addFilter(new StatFilter());
    // WallFilter的功能是防御SQL注入攻击
    WallFilter wallDefault = new WallFilter();
    wallDefault.setDbType("mysql");
    druidPlugin.addFilter(wallDefault);

    druidPlugin.setInitialSize(prop.getInt("db.default.poolInitialSize"));
    druidPlugin.setMaxPoolPreparedStatementPerConnectionSize(prop.getInt("db.default.poolMaxSize"));
    druidPlugin.setTimeBetweenConnectErrorMillis(prop.getInt("db.default.connectionTimeoutMillis"));

    pluginLoader.add(druidPlugin);
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
    activeRecordPlugin.addIncludePaths("org.anyshop.model");//扫描orm映射
    activeRecordPlugin.setShowSql(true);

    pluginLoader.add(activeRecordPlugin);
  }

  public void configInterceptor(InterceptorLoader interceptorLoader) {
    //权限拦截器
    interceptorLoader.add(new SecurityInterceptor(new MyAuthenticateService()));
    //事务的拦截器 @Transaction
    interceptorLoader.add(new TransactionInterceptor());
  }

  public void configHandler(HandlerLoader handlerLoader) {
    //cors 跨域拦截
    handlerLoader.add(new CORSHandler("*"));
  }
}
