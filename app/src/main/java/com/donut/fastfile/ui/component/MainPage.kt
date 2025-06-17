package com.donut.fastfile.ui.component

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.donut.fastfile.currentActivity
import com.donut.fastfile.serverAddress
import com.donut.fastfile.ui.theme.colorScheme
import com.donut.fastfile.util.copyToClipboard
import com.donut.fastfile.util.file.FileDataLog
import com.donut.fastfile.util.file.InfoText
import com.donut.fastfile.util.file.deleteUploadLog
import com.donut.fastfile.util.file.selectAndUploadFile
import com.donut.fastfile.util.file.showFileResult
import com.donut.fastfile.util.file.uploadLogs
import com.donut.fastfile.util.formatFileSize
import com.donut.fastfile.util.formatTime
import java.util.Date


@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun FileCard(fileDataLog: FileDataLog, showDate: Boolean = true, longClick: () -> Unit = {}) {
    HorizontalDivider()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(107, 218, 246, 0),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {
                    longClick()
                }
            ) {
                showFileResult(fileDataLog)
            }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = fileDataLog.name.trim(),
                color = colorScheme.primary,
                fontSize = 16.sp,
            )
            FlowRow(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoText(key = "大小: ", value = formatFileSize(fileDataLog.size))
                if (showDate) {
                    Text(
                        text = formatTime(Date(fileDataLog.time)),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectAndUploadFile()
            }, modifier = Modifier.padding(10.dp, 50.dp)) {
                Icon(Icons.Filled.Add, "Upload File")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp, 10.dp)
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "局域网地址: ${serverAddress}",
                color = colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            serverAddress.copyToClipboard()
                        })
            )
            if (uploadLogs.isEmpty()) {
                Text(text = "点击右下角上传文件", color = colorScheme.primary, fontSize = 20.sp)
            }
            if (uploadLogs.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = "上传历史",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(0.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(0.dp, 550.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            val logs = uploadLogs.reversed()
                            items(uploadLogs.size) { index ->
                                FileCard(logs[index]) {
                                    deleteUploadLog(logs[index])
                                }
                            }
                        }
                    }
                }
            }
            Text(
                color = colorScheme.primary,
                text = "https://github.com/InvertGeek/FastFile",
                modifier = Modifier.clickable {
                    openGithubLink()
                }
            )
        }

    }

}

fun openGithubLink() {
    val intent =
        Intent(
            Intent.ACTION_VIEW,
            "https://github.com/InvertGeek/FastFile".toUri()
        )
    currentActivity.startActivity(intent)
}
