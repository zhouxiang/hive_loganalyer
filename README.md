# hive_loganalyer
this is a analyer for hive on spark.
it  analyse hive on spark's output log directory,and generate a report.
so,you can find how many cores used by your sql job ,and find when the lowest cores ,so give you a 
bluepottin on how to optimize your sql .

这是一个分析hive on spark 执行日志的小工具，分析完成后会生成一个所有脚本使用核数随时间变化的报告。
从这个报告中，你可以看到你自己的脚本对集群的核数使用情况，并通过这个报告可以直观的看出你的所有任务是否充分利用集群资源。
从而调整你的代码并行度等，优化sql
