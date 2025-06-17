package com.dex.fastfile

import com.alibaba.fastjson2.into
import com.donut.fastfile.util.file.FileDataLog
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        runBlocking {
            val foo: List<FileDataLog> =
                "[{\"name\":\"Screenshot_2025-06-15-10-23-45-961_com.tencent.mobileqq.png\",\"size\":43290,\"time\":\"2025-06-17 08:11:24.965\",\"url\":\"https://qidian-bear-img-ol-1251316161.cos.ap-shanghai.myqcloud.com/qidian/im/file/7603f878-d9ae-47af-9131-df43df38b155\"}]".into()
            println(foo)
        }
    }
}