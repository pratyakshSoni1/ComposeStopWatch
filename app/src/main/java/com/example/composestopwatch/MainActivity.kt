package com.example.composestopwatch

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composestopwatch.ui.theme.ComposeStopWatchTheme
import com.example.composestopwatch.ui.theme.mainBackgroundColor
import com.example.composestopwatch.ui.theme.statusBarColor
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    private var watchIsStop:Boolean = true
    private var watchStartMills:Long?=null
    private var watchTimeDifferenceFromStartTime:Long=0
    private var LIFE_TAG = "WATCH_LIFECYCLE"

    private var currentWatchTimeMills = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {

            window.statusBarColor = mainBackgroundColor.toArgb()

            val stopWatchDependencyTime = remember {
                mutableStateOf(0L)
            }

            ComposeStopWatchTheme {
                // A surface container using the 'background' color from the theme

                Column(
                    modifier= Modifier
                        .fillMaxSize()
                        .background(mainBackgroundColor) , verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(64.dp))
                    
                    MainScreen(
                        onWatchStart = { onWatchStart(stopWatchDependencyTime) },
                        onWatchStop = {  onWatchStop(stopWatchDependencyTime) },
                        currentTimeSpent = stopWatchDependencyTime.value ,
                        onWatchPause = {  onWatchPause{ onWatchResume(stopWatchDependencyTime) } }
                    )

                }


            }
        }
    }

    fun onWatchStart(watchTimeStateValue:MutableState<Long> ) {

        if (watchIsStop && watchStartMills == null) {
            watchIsStop = false
            watchStartMills = SystemClock.uptimeMillis()
            CoroutineScope(Dispatchers.IO).launch {
                while (!watchIsStop) {
                    currentWatchTimeMills = SystemClock.uptimeMillis()
                    watchTimeStateValue.value = currentWatchTimeMills - watchStartMills!!
                    delay(500)
                }
            }
        } else if (watchIsStop && watchStartMills != null) {
            onWatchResume(watchTimeStateValue)
        }
    }


    fun onWatchResume( watchTimeStateValue: MutableState<Long> ){
        watchIsStop = false
        watchTimeDifferenceFromStartTime += (SystemClock.uptimeMillis() - currentWatchTimeMills)
        Log.e(LIFE_TAG,"onResume Called : watchStartMills:$watchStartMills , watchIsStop:$watchIsStop , currentMills:${watchTimeStateValue.value}")

        CoroutineScope(Dispatchers.IO).launch {
            while (!watchIsStop) {
                currentWatchTimeMills = SystemClock.uptimeMillis()
                watchTimeStateValue.value =   (currentWatchTimeMills - watchTimeDifferenceFromStartTime)- watchStartMills!!
                Log.e(
                    LIFE_TAG,
                    "onResume Coroutine : watchStartMills:$watchStartMills , watchIsStop:$watchIsStop ,differ:$watchTimeDifferenceFromStartTime currentMills:${watchTimeStateValue.value}"
                )
                delay(500)
            }        }
    }

    fun onWatchPause(onResume:()->(Unit)){
        Log.e(LIFE_TAG,"onPause Called : watchStartMills:$watchStartMills , watchIsStop:$watchIsStop")
        if(watchIsStop){
            onResume()
        }else{
            watchIsStop = true
            Log.e(LIFE_TAG,"onPause Watch Paused : watchStartMills:$watchStartMills , watchIsStop:$watchIsStop ")
        }
    }


    fun onWatchStop(watchTimeStateValue:MutableState<Long>){

        Log.e(LIFE_TAG,"onstop Watch Reset : watchStartMills:$watchStartMills , watchIsStop:$watchIsStop ")

        watchIsStop = true
        watchStartMills = null
        watchTimeStateValue.value = 0
        watchTimeDifferenceFromStartTime=0

    }


}


@Composable
fun MainScreen(onWatchStart:()->(Unit), onWatchStop:()->(Unit), onWatchPause:()->(Unit), currentTimeSpent:Long){


    Column(modifier=Modifier.fillMaxSize()) {

        Row(modifier= Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.65f) ,

            horizontalArrangement = Arrangement.Center ,
            verticalAlignment = Alignment.CenterVertically

            ) {

            var hoursSpent by remember {
                mutableStateOf(0L)
            }

            var minutesSpent by remember {
            mutableStateOf(0L)
            }

            var secondsSpent by remember {
                mutableStateOf(0L)
            }

            LaunchedEffect(key1 = currentTimeSpent){
                hoursSpent =   (((currentTimeSpent/1000)/60)/60)
                minutesSpent = (((currentTimeSpent/1000)/60).toInt() %60).toLong()
                secondsSpent = (((currentTimeSpent/1000).toInt() %60)).toLong()

                Log.e("TAG" , "0$hoursSpent h : 0$minutesSpent m : $secondsSpent s")
            }

            CircularProgressBar(radius = 50.dp, totalNumber = 60, percentage = hoursSpent.toFloat(), strokeWidth = 8.dp, fontSize = 39.sp , textSuffix = "h") //HourHand
            CircularProgressBar(radius = 39.dp, totalNumber = 60, percentage = minutesSpent.toFloat(), strokeWidth = 7.dp,  fontSize = 30.sp, textSuffix = "m") //Minutehand
            CircularProgressBar(radius = 33.dp, totalNumber = 60, percentage = secondsSpent.toFloat(), strokeWidth = 5.dp,  fontSize = 25.sp, textSuffix = "s") // SecondHand

        }
        
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight() ) {

            Box(modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth() , contentAlignment = Alignment.BottomCenter) {
                Image(
                    painter = painterResource(id = R.drawable.button_image_card),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds ,
                    modifier= Modifier
                        .fillMaxWidth()
                        .padding(bottom = 59.dp)
                )
            }
            Column(modifier=Modifier.fillMaxWidth()){
                Spacer(
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth()
                )

                Button(
                    onClick = { onWatchStart() }
                    , colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
                    modifier = Modifier.align(Alignment.CenterHorizontally) ,
                    shape = CircleShape
                ) {

                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start watch Timer",
                        tint = Color.White,
                        modifier = Modifier.padding(1.dp)
                    )

                }

                Spacer(
                    modifier = Modifier
                        .height(22.dp)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {

                    Button(onClick ={ onWatchPause() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                        shape = CircleShape) {

                        Text(text = "II", color = Color.White, fontWeight = FontWeight(1000), fontSize = 22.sp)

                    }

                    Spacer(modifier = Modifier.width(128.dp))

                    Button(onClick = { onWatchStop() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        shape = CircleShape) {

                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Reset watch Timer",
                            tint = Color.White,
                        )

                    }
                }
            }
        }
    }

}


@Composable
fun CircularProgressBar(
    radius:Dp,
    totalNumber:Int,
    percentage:Float,
    animationDelay:Int = 0,
    animationDuration:Int = 200,
    barColor: Color = Color.Green,
    textColor:Color = Color.Green ,
    strokeWidth:Dp ,
    fontSize:TextUnit ,
    textSuffix:String=""

    ){

    var animationPlayed by remember{
        mutableStateOf(false)
    }

    val currentPercentage = animateFloatAsState(
        targetValue = percentage ,
        animationSpec = spring(
            dampingRatio = 5f ,
        )
    )

    Box(modifier=Modifier.size((radius * 2.5f)) , contentAlignment = Alignment.Center){

        Canvas(modifier = Modifier.size(radius * 2f)){

            drawArc(
                color = Color(0x75FF9E9E),
                startAngle = -90f ,
                sweepAngle = 360f ,
                useCenter = false ,
                style = Stroke(width =  strokeWidth.toPx(),cap = StrokeCap.Round )
            )

        }

        Canvas(modifier = Modifier.size(radius * 2f)){

            drawArc(
                color = barColor ,
                startAngle = -90f ,
                sweepAngle = 6 * currentPercentage.value ,
                useCenter = false ,
                style = Stroke(width =  strokeWidth.toPx(),cap = StrokeCap.Round )
            )

        }
        
        Text( text = "${currentPercentage.value.toInt()} $textSuffix",
            color = Color.White ,
            fontSize = fontSize ,
            fontWeight = FontWeight.Bold
        )

    }


}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeStopWatchTheme {

    }
}



