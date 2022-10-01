package com.example.jettip

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jettip.components.InputField
import com.example.jettip.components.InputFieldSmall
import com.example.jettip.ui.theme.JetTipTheme
import com.example.jettip.util.calculateTotalPerPerson
import com.example.jettip.util.calculateTotalTip
import com.example.jettip.widgets.RoundIconButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MainContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    JetTipTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun TopHeader(totalPerPerson: Double = 0.0) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(150.dp)
            .clip(shape = CircleShape.copy(all = CornerSize(12.dp))),
        color = Color(0xFFE9D7F7)
//            .clip(shape = RoundedCornerShape(corner = CornerSize(12.dp)))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val total = "%.2f".format(totalPerPerson)
            Text(text = "Total per Person", style = MaterialTheme.typography.h5)
            Text(
                text = "$$total",
                style = MaterialTheme.typography.h3,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun MainContent() {
//  ---State Hoisting---
    val splitByState = remember {
        mutableStateOf(1)
    }
//    val splitRange = IntRange(start = 1, endInclusive = 100)
    val tipAmountState = remember {
        mutableStateOf(0.0)
    }
    val totalPerPersonState = remember {
        mutableStateOf(0.0)
    }
    val tipPercentageState = remember {
        mutableStateOf(0)
    }

    BillForm(
        splitByState = splitByState,
        tipAmountState = tipAmountState,
        totalPerPersonState = totalPerPersonState,
        tipPercentageState = tipPercentageState
    ) { billAmt ->
        Log.d("amt", "MainContent: $billAmt")
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BillForm(
    modifier: Modifier = Modifier,
    splitRange: IntRange = 1..100,
    splitByState: MutableState<Int>,
    tipAmountState: MutableState<Double>,
    tipPercentageState: MutableState<Int>,
    totalPerPersonState: MutableState<Double>,
    onValueChange: (String) -> Unit = {}
) {
    val customTipState = remember {
        mutableStateOf("")
    }
    val totalBillState = remember {
        mutableStateOf("")
    }
    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty()
    }
    val validSliderState = remember(customTipState.value) {
        customTipState.value.isNotEmpty()
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val sliderPositionState = remember {
        mutableStateOf(0f)
    }
    var tipPercentage = (sliderPositionState.value * 100 / 4).toInt()

    val patternNumber = remember { Regex("^\\d+\$") }

    Column() {

        TopHeader(totalPerPerson = totalPerPersonState.value)

        Surface(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(corner = CornerSize(8.dp)),
            border = BorderStroke(width = 2.dp, color = Color.LightGray)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                InputField(
                    valueState = totalBillState,
                    labelId = "Enter Bill",
                    enabled = true,
                    isSingleLine = true,
                    onAction = KeyboardActions {
                        if (!validState) return@KeyboardActions
                        onValueChange(totalBillState.value.trim())   // the value for trailing Lambda
                        keyboardController?.hide()
                        totalPerPersonState.value = calculateTotalPerPerson(
                            totalBill = totalBillState.value.toDouble(),
                            splitBy = splitByState.value,
                            tipPercentage = tipPercentage
                        )
                    }
                )
                if (validState) {
                    //  ---Split Row---
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            "Split",
                            modifier = Modifier.align(alignment = Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(120.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 3.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            RoundIconButton(
                                modifier = modifier,
                                imageVector = Icons.Default.Remove,
                                onClick = {
                                    splitByState.value =
                                        if (splitByState.value > 1) splitByState.value - 1
                                        else 1
                                    totalPerPersonState.value = calculateTotalPerPerson(
                                        totalBill = totalBillState.value.toDouble(),
                                        splitBy = splitByState.value,
                                        tipPercentage = tipPercentage
                                    )
                                })
                            Text(
                                text = "${splitByState.value}",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 12.dp, end = 12.dp)
                            )
                            RoundIconButton(
                                modifier = modifier,
                                imageVector = Icons.Default.Add,
                                onClick = {
                                    if (splitByState.value < splitRange.last) {
                                        splitByState.value += 1
                                    }
                                    totalPerPersonState.value = calculateTotalPerPerson(
                                        totalBill = totalBillState.value.toDouble(),
                                        splitBy = splitByState.value,
                                        tipPercentage = tipPercentage
                                    )
                                })
                        }
                    }

                    // ---Tip Row---
                    Row(modifier = Modifier.padding(horizontal = 3.dp, vertical = 12.dp)) {
                        Text(text = "Tip", modifier = Modifier.align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(200.dp))
                        Text(
                            text = "$ ${tipAmountState.value}",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.h6
                        )
                    }
                    //  ---Slider---
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (tipPercentage in 1..25) {
                            Text(text = "$tipPercentage %", style = MaterialTheme.typography.h6)

                        } else {
                            Text(
                                text = "spacer",
                                style = MaterialTheme.typography.h6,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Slider(value = sliderPositionState.value, onValueChange = { newVal ->
                            sliderPositionState.value = newVal
                            customTipState.value = ""
                            tipAmountState.value = calculateTotalTip(
                                totalBill = if (totalBillState.value.isNotEmpty()) totalBillState.value.toDouble() else 0.0,
                                tipPercentage = tipPercentage
                            )
                            totalPerPersonState.value = calculateTotalPerPerson(
                                totalBill = totalBillState.value.toDouble(),
                                splitBy = splitByState.value,
                                tipPercentage = tipPercentage
                            )
                        },
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            steps = 7,
                            onValueChangeFinished = {
                            })
                    }

                    // ---Custom Tip---
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InputFieldSmall(
                            modifier = modifier,
                            valueState = customTipState,
                            labelId = "Custom Tip",
                            enabled = true,
                            isSingleLine = true,
                            onAction = KeyboardActions {
                                if (!validState) return@KeyboardActions
                                onValueChange(customTipState.value.trim())   // the value for trailing Lambda
                                keyboardController?.hide()
                                if (customTipState.value.isEmpty()) {
                                    customTipState.value = "0"
                                }
                                totalPerPersonState.value = calculateTotalPerPerson(
                                    totalBill = totalBillState.value.toDouble(),
                                    splitBy = splitByState.value,
                                    tipPercentage = customTipState.value.toInt()
                                )
                                tipAmountState.value =
                                    totalBillState.value.toDouble() * customTipState.value.trim()
                                        .toDouble() / 100
                                tipPercentageState.value = customTipState.value.toInt()
                                tipPercentage = customTipState.value.toInt()
                                sliderPositionState.value = 0.0f
                            }
                        )
                    }
                } else {
                    Box() {}
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JetTipTheme {
        MyApp {
            Text(text = "Hello!")
        }
    }
}