package com.sergey5588.aiassistant

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxHeight


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sergey5588.aiassistant.ui.theme.AIAssistantTheme

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject


class Message(val isAI:Boolean, val content:String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIAssistantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    innerPadding.toString()
                    ChatLayout()

                }
            }
        }
    }
}


@Preview
@Composable
fun ChatLayout() {
    val messages = remember { mutableStateListOf<Message>() } // Store chat messages
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val scrollCoroutineScope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    val chatScrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).imePadding()) {
        // Scrollable column for messages

        Column(
            modifier = Modifier
                .weight(1f) // Take up remaining space
                .fillMaxWidth()
                .verticalScroll(chatScrollState)
                .padding(0.dp, 20.dp)

        ) {

            for (msg in messages) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = if (msg.isAI) Arrangement.Start else Arrangement.End
                ) {
                    // Add a Box with rounded background around the Text
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = if (msg.isAI) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp) // Inner padding for text
                    ) {
                        SelectionContainer {
                            Text(
                                text = msg.content,
                                style = TextStyle(color = MaterialTheme.colorScheme.inverseSurface)
                                //color = MaterialTheme.colorScheme.inverseSurface
                            )
                        }
                    }
                }
                LaunchedEffect(Unit) {

                    scrollCoroutineScope.launch {

                        chatScrollState.animateScrollTo(chatScrollState.maxValue)
                    }
                }
            }

        }


        // Row for input field and send button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f).padding(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(onClick = {
                if (inputText.text.isNotBlank()) {
                    val msg = inputText.text
                    messages.add(Message(false,msg)) // Add message to the list

                    coroutineScope.launch {

                        messages.add(Message(true, getResponse(msg)))
                    }
                    inputText = TextFieldValue("") // Clear input field
                }
            }) {
                Text("Send")
            }
        }
    }
}
@Serializable
data class ans(val text:String)
suspend fun getResponse(prompt:String):String {

    val client = HttpClient(CIO) {
        engine {
            requestTimeout = 0
        }
    }


    val response: HttpResponse = client.post("http://g4f.oxnack.ru/send_message") {
        contentType(ContentType.Application.Json)
        setBody("{\"passwd\":\"0000\", \"text_promt\":\"$prompt\", \"img_promt\":\"\"}")

    }
    val resp = Json.decodeFromString<ans>(response.bodyAsText())
    return resp.text

}