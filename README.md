# UpDB
Database migration for GreenDao and Android

# 具体思路
- 1.将初始的表重命名临时表 ALTER tablename RENAME TO newtablename.
- 2.创建和GreenDao一样的表(很重要,网上都是创建和旧表相同),将新字段设置默认值0
- 3.将就表整表复制用2中建的表用Select
- 4.最后将2中的表中的值复制给GreenDao新建的表.
- 5.删除临时表(通过改名也是删除旧表)

> 因为GreenDao没有在字段上赋值初值的问题.它在建表的时候,除了TEXT 都是 NOT NULL.所有很多时候回出错.
