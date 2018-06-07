package org.jamescowan.bluetooth.echo.view

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.jamescowan.bluetooth.echo.Constants
import org.jamescowan.bluetooth.echo.R
import org.jamescowan.bluetooth.echo.client.GattClient
import org.jamescowan.bluetooth.echo.client.IGattClient
import org.jamescowan.bluetooth.echo.client.IGattClientListener
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class ConnectActivity : AppCompatActivity() {

    private lateinit var gattClient: IGattClient
    private var connected = false;
    private lateinit var view: ConnectActivityUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ConnectActivity.ConnectActivityUI()
        view.setContentView(this)

        val device: BluetoothDevice = intent.getParcelableExtra(Constants.BLUETOOTH_ARG);

        gattClient = GattClient(Constants.ServiceUUID(), Constants.CharacteresticUUID(), object : IGattClientListener {
            override fun isConnected() {
                connected = true
                updateConnectionStatus()
            }

            override fun isClosed() {
                connected = false
                updateConnectionStatus()
            }

            override fun response(message: String) {
                updateResponse(message)
            }
        })
        gattClient.connect(this, device)
    }

    override fun onDestroy() {
        super.onDestroy()

        gattClient.close()
    }

    private fun getConnectionStatus(): String {
        return if (connected) "Connected" else "Not connected"
    }

    private fun sendMessage() {

        if (!connected) {
            return;
        }

        val text: String = this.view.input.text.toString()

        if (text.length == 0) {
            Toast.makeText(this, R.string.error_no_text, Toast.LENGTH_SHORT).show()
        }

        gattClient.write(text);

        this.view.input.text.clear()
    }

    private fun updateConnectionStatus() {
        this@ConnectActivity.runOnUiThread(Runnable {
            view.status.text = getConnectionStatus()
        });
    }

    private fun updateResponse(text: String) {
        this@ConnectActivity.runOnUiThread(Runnable {
            view.response.setText(text)
        });
    }

    // inner classes
    // inner UI class

    class ConnectActivityUI() : AnkoComponent<ConnectActivity> {
        lateinit var input: EditText
        lateinit var response: EditText
        lateinit var status: TextView

        override fun createView(ui: AnkoContext<ConnectActivity>) = with(ui) {

            verticalLayout {
                lparams(width = matchParent, height = matchParent) {
                    weightSum = 1F
                }

                verticalLayout() {

                    status = textView(owner.getConnectionStatus()) {
                        gravity = Gravity.CENTER
                        padding = 15
                        textSize = 25f
                        typeface = Typeface.DEFAULT_BOLD
                    }
                }.lparams(width = matchParent, height = wrapContent)

                view {
                    backgroundColor = Color.BLACK
                }.lparams(width = matchParent, height = dip(1))

                view {
                }.lparams(width = matchParent, height = 0, weight = 0.05F)

                verticalLayout {
                    verticalLayout {

                        gravity = Gravity.CENTER
                        textView("Message") {
                            gravity = Gravity.CENTER
                            padding = 15
                            textSize = 15f
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        linearLayout() {
                            gravity = Gravity.CENTER
                            lparams(width = matchParent, height = wrapContent) {
                                weightSum = 1F
                            }
                            input = editText() {
                                gravity = Gravity.TOP
                                hint = "Enter message"
                                lines = 6
                                maxLines = 6
                                padding = 5
                                singleLine = false
                                setBackgroundResource(R.drawable.shape)
                            }.lparams(width = 0, height = wrapContent, weight = 0.5F)
                        }

                        button(text = "Send") {
                            gravity = Gravity.CENTER
                            onClick {
                                owner.sendMessage();
                            }
                        }.lparams(width = wrapContent, height = matchParent)
                    }.lparams(width = matchParent, height = wrapContent)

                    verticalLayout {
                        gravity = Gravity.CENTER
                        textView("Response") {
                            gravity = Gravity.CENTER
                            padding = 15
                            textSize = 15f
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        linearLayout() {
                            gravity = Gravity.CENTER
                            lparams(width = matchParent, height = wrapContent) {
                                weightSum = 1F
                            }
                            response = editText() {
                                gravity = Gravity.TOP
                                hint = ""
                                lines = 6
                                maxLines = 6
                                padding = 5
                                singleLine = false
                                setBackgroundResource(R.drawable.shape)
                            }.lparams(width = 0, height = wrapContent, weight = 0.5F)
                        }
                    }.lparams(width = matchParent, height = wrapContent)
                }.lparams(width = matchParent, height = 0, weight = 0.85F)

                verticalLayout {
                    button(text = "Quit") {

                        onClick {
                            owner.finish();
                        }
                    }
                }.lparams(width = matchParent, height = 0, weight = 0.10F)
            }
        }
    }

}