# es-migrate-tool
背景：
    ES从高版本回迁至低版本，远程reindex方式只能在低版本执行，redindex命令执行时低版本的es会向高版本的ES发送请求，没有携带高版本ES6.x需要
的http请求头Content-Type:application/json，所以执行失败！

设计：
    第一个版本用scroll去批量拉取高版本ES的数据，默认单个slice批量拉取，同步每次批量往目的index里写数据。
    
缺点：
    对于数据量较大的index无法快速同步完毕。后期考虑支持多slice。    
