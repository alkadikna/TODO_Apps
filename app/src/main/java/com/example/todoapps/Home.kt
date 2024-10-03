package com.example.todoapps

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.todoapps.ui.theme.TODOAppsTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

private lateinit var auth: FirebaseAuth;
private lateinit var database: FirebaseDatabase;

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp(modifier: Modifier = Modifier, navController: NavHostController) {
    var taskTitle by remember { mutableStateOf("") }
//    val tasks = remember { mutableStateListOf<Task>() }
    val tasks = remember { mutableStateListOf<Pair<Task, String>>() }
    var userEmail: String = ""
    var userId: String = ""
    val context = LocalContext.current
    val auth = Firebase.auth
    val database = Firebase.database("https://todo-apps-5f42d-default-rtdb.firebaseio.com/")

    if(!LocalInspectionMode.current){
        val currentUser = auth.currentUser
        if(currentUser !=null){
            userEmail = currentUser.email.toString()
            userId = currentUser.uid
        }
    }

    // Create
    fun saveTask() {
        val newTask = Task(title = taskTitle, isChecked = false)

        val user = auth.currentUser
        if (user != null) {
            val myRef = database.getReference("notes").child(user.uid).push()
            myRef.setValue(newTask).addOnSuccessListener {
                taskTitle = ""
                Toast
                    .makeText(context, "New task added successfully", Toast.LENGTH_LONG)
                    .show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to add task", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show()
        }
    }

    // Read
    fun loadTasks() {
        val user = auth.currentUser
        if (user != null) {
            val myRef = database.getReference("notes").child(user.uid)
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        tasks.clear()
                        for (taskSnapshot in snapshot.children) {
                            val task = taskSnapshot.getValue(Task::class.java)
                            task?.let {
                                tasks.add(Pair(it, taskSnapshot.key ?: ""))
                            }
                        }
                    } else {
                        Log.d("LoadTasks", "No data found")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("LoadTasks", "Failed to load tasks: ${error.message}")
                    Toast.makeText(context, "Failed to load tasks: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    // Update
    fun updateTask(task: Task, key: String) {
        val user = auth.currentUser
        if (user != null) {
            val myRef = database.getReference("notes").child(user.uid).child(key)
            val updates = hashMapOf<String, Any?>(
                "title" to task.title,
                "isChecked" to task.isChecked
            )

            myRef.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(context, "${key} updated successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { error ->
                Toast.makeText(context, "Failed to update task: ${error.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Task ID is invalid", Toast.LENGTH_LONG).show()
        }
    }

    // Delete
    fun deleteTask(task: Task, key: String) {
        val user = auth.currentUser
        if (user != null) {
            val myRef = database.getReference("notes").child(user.uid).child(key)
            myRef.removeValue().addOnSuccessListener {
                tasks.removeAll { it.first == task }
                Toast.makeText(context, "${task.title} deleted successfully", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete task", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadTasks()
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xFF81248A),
                    titleContentColor = Color.White,
                ),
                title = { Text(text = "TODO APP", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selamat datang, ",
                modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp)
            )

            Text(
                text = userEmail,
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                fontSize = 25.sp,
                color = Color(0xFFFD8C00)
            )

            Text(
                text = userId,
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                fontSize = 25.sp,
                color = Color(0xFFFD8C00)
            )

            Button(onClick = {
                Firebase.auth.signOut()
                navController.navigate("login")
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF81248A),
                )
            ) {
                Text("Logout", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier.height(5.dp))

            TextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.LightGray),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                if (taskTitle.isNotEmpty()) {
                    saveTask()
                }
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFD8C00),
                )
            ) {
                Text("Save", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(tasks) { (task, key) ->
                    TaskRow(
                        task = task,
                        onCheckedChange = { checked -> task.isChecked = checked},
                        onDeleteTask = {
                            deleteTask(task, key)
                        },
                        onUpdateTask = {
                            updateTask(task, key)
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteTask: () -> Unit,
    onUpdateTask: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var isChecked by remember { mutableStateOf(task.isChecked) }
        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                onCheckedChange(checked)
                onUpdateTask()
            },
            colors = CheckboxDefaults.colors(checkmarkColor = Color(0xFFFFF0D1), checkedColor = Color(0xFFFD8C00))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = task.title,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDeleteTask) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Task",
                tint = Color(0xFF81248A)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TODOAppsTheme {
        TodoApp(navController = rememberNavController())
    }
}