package com.pushupminutes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pushupminutes.R

@Composable
fun TimesUpScreen(onBackToPushups: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arm),
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(text = stringResource(R.string.times_up_title), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(R.string.times_up_body), style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onBackToPushups, modifier = Modifier.padding(top = 24.dp)) {
            Text(text = stringResource(R.string.do_pushups))
        }
    }
}
