package com.example.myapplication1

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarConnectionSettings
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.MagnificationParameter
import com.starmicronics.stario10.starxpandcommand.PrinterBuilder
import com.starmicronics.stario10.starxpandcommand.StarXpandCommandBuilder
import com.starmicronics.stario10.starxpandcommand.printer.Alignment
import com.starmicronics.stario10.starxpandcommand.printer.CutType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    class WebAppInterface
    /** Instantiate the interface and set the context  */ internal constructor(var mContext: MainActivity) {
        @JavascriptInterface // must be added for API 17 or higher
        fun print(data: String?) {
            mContext.onPressPrintButton(data!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.wv)
        val webSettings = webView.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true



        webView.addJavascriptInterface(WebAppInterface(this), "Android");
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false;
            }
        }

        webView.loadUrl("http://192.168.1.5/")


    }

    fun onPressPrintButton(data: String) {
        Log.d("data", data)
        val jObject = JSONObject(data)
        val total= jObject.getString("total")
        val tip= jObject.getString("tip")
        val foodsList = jObject.getJSONArray("receipt_items")
        val foodsPrint: PrinterBuilder = PrinterBuilder();
        for (i in 0 until foodsList.length()) {
            try {
                val oneFood = foodsList.getJSONObject(i)
                Log.v("", oneFood.toString())
                val orderedCount = oneFood.getString("ordered_count").toInt();
                Log.v("", orderedCount.toString())
                val name = oneFood.getString("food_name");
                Log.v("", name.toString())
                val price = oneFood.getString("food_price").toFloat();
                Log.v("", price.toString())

                val totalPrice = orderedCount * price;
                Log.v("", totalPrice.toString())

                val spaceLength= 32- 2-orderedCount.toString().length-name.length-totalPrice.toString().length
                foodsPrint.actionPrintText(orderedCount.toString() + "x $name"+" ".repeat(if(spaceLength>0) spaceLength else 0)+totalPrice.toString()+'\n')

            } catch (e: JSONException) {
                // Oops
            }

        }
        // Specify your printer connection settings.
        val settings = StarConnectionSettings(InterfaceType.Bluetooth, "00:11:62:1A:1E:83")
        val printer = StarPrinter(settings, applicationContext)

        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.Default + job)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val tf: DateFormat = SimpleDateFormat("HH:mm:ss")
        val currentTime: Date = Calendar.getInstance().time

        scope.launch {
            try {

                printer.openAsync().await()

                val builder = StarXpandCommandBuilder()
                builder.addDocument(
                    DocumentBuilder()
                        .addPrinter(
                            PrinterBuilder()
                                .styleAlignment(Alignment.Center)
                                .add(
                                    PrinterBuilder()
                                        .styleBold(true)
                                        .styleMagnification(MagnificationParameter(2, 2))
                                        .actionPrintText(
                                            "ZAFERUW\n"
                                        )
                                )
                                .add(
                                    PrinterBuilder()
                                        .styleInvert(true)
                                        .actionPrintText(
                                            "Persian Cuisine\n"
                                        )
                                )
                                .actionPrintText(
                                    "Burgwal 48. 2611 GJ Delft\n" +
                                            "Phone +31 6 20266589\n" +
                                            "http://www.zaferuw.nl\n" +
                                            "zaferuw.cuisine@gmail.com\n" +
                                            "Company no: 76577937\n"
                                )
                                .add(
                                    PrinterBuilder()
//                                        .styleUnderLine(true)
                                        .actionPrintText(
                                            "--------------------------------\n"
                                        )
                                )
                                .styleAlignment(Alignment.Left)
                                .add(
                                    PrinterBuilder()
//                                        .styleBold(true)
//                                        .styleMagnification(MagnificationParameter(2, 2))
                                        .actionPrintText(
                                            "Date:" + df.format(currentTime) + "    Time:" + tf.format(
                                                currentTime
                                            )
                                        )
                                )
                                .add(
                                    PrinterBuilder()
//                                        .styleUnderLine(true)
                                        .actionPrintText(
                                            "--------------------------------\n"
                                        )
                                )
                                .add(foodsPrint)
                                .actionPrintText(
                                    "--------------------------------\n"
                                )
                                .add(
                                    PrinterBuilder()
                                        .styleBold(true)
//                                        .styleMagnification(MagnificationParameter(2, 2))
                                        .actionPrintText(
                                            "Subtotal"+" ".repeat(23-total.length)+"€$total\n"
                                        )
                                )
                                .actionPrintText(
                                    "--------------------------------\n"
                                )
                                .add(
                                    PrinterBuilder()
                                        .styleBold(true)
//                                        .styleMagnification(MagnificationParameter(2, 2))
                                        .actionPrintText(
                                            "Tips"+" ".repeat(27-tip.length)+"€$tip\n"
                                        )
                                )
                                .add(
                                    PrinterBuilder()
                                        .styleBold(true)
//                                        .styleMagnification(MagnificationParameter(2, 2))
                                        .actionPrintText(
                                            "Total"+" ".repeat(27-2*(1+total.length))
                                        )
                                )
                                .add(
                                    PrinterBuilder()
                                        .styleBold(true)
                                        .styleMagnification(MagnificationParameter(2, 2))
                                        .actionPrintText(
                                            "€$total\n"
                                        )
                                )

                                .actionCut(CutType.Partial)
                        )
                )

                // ...
                val commands = builder.getCommands()
                printer.printAsync(commands).await()
                // Print.
            } catch (e: Exception) {
                // Exception.
                Log.d("Printing", "${e.message}")
            } finally {
                // Disconnect from the printer.
                printer.closeAsync().await()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}