package com.example.userlistactivity.matey.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.userlistactivity.matey.database.Bill
import com.example.userlistactivity.matey.database.Chore
import com.example.userlistactivity.matey.database.Friend
import com.example.userlistactivity.matey.model.BillCategory
import com.example.userlistactivity.matey.navigation.Screen
import com.example.userlistactivity.matey.ui.theme.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.delay

// --- NEW DASHBOARD ---
@Composable
fun DashboardScreen(
    bills: List<Bill>,
    friends: List<Friend>,
    navController: NavHostController
) {
    // Calculate total amount owed to you across all bills (only non-settled ones)
    val owesYou = bills.filter { it.paidBy == "You" && !it.isSettled }.sumOf { bill ->
        val names = if (bill.splitWith.isEmpty()) emptyList() else bill.splitWith.split(", ")
        val paidNames = if (bill.paidFriends.isEmpty()) emptyList() else bill.paidFriends.split(", ")
        val share = bill.amount / (names.size + (if (bill.includeMe) 1 else 0))
        val unpaidCount = names.count { !paidNames.contains(it) }
        share * unpaidCount
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(15.dp))
        Text(
            text = "Split",
            color = AppWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.height(30.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            SummaryCard("Owes you", owesYou, Modifier.fillMaxWidth(1f))
        }
        
        Spacer(Modifier.height(40.dp))

        Card(
            shape = RoundedCornerShape(40.dp, 40.dp, 0.dp, 0.dp),
            colors = CardDefaults.cardColors(containerColor = AppWhite),
            modifier = Modifier.fillMaxSize()
        ) {
            val pendingBills = bills.filter { !it.isSettled }
            if (pendingBills.isEmpty() && friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "opps no one owe you for now",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
                ) {
                    if (pendingBills.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pending Bills", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { navController.navigate(Screen.Bills.route) }) {
                                    Text("View All", color = AppOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        items(pendingBills.take(2)) { bill ->
                            PendingBillItem(bill = bill, onClick = { navController.navigate(Screen.BillDetail.createRoute(bill.id)) })
                        }
                    }

                    if (friends.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Friends", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { navController.navigate(Screen.Friends.route) }) {
                                    Text("View All", color = AppOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        items(friends.take(2)) { friend ->
                            // Calculate actual amount this friend owes
                            val amountOwed = bills.filter { it.paidBy == "You" && !it.isSettled && it.splitWith.split(", ").contains(friend.name) }
                                    .sumOf { bill ->
                                        val names = bill.splitWith.split(", ")
                                        val paidNames = if (bill.paidFriends.isEmpty()) emptyList() else bill.paidFriends.split(", ")
                                        if (paidNames.contains(friend.name)) {
                                            0.0
                                        } else {
                                            bill.amount / (names.size + (if (bill.includeMe) 1 else 0))
                                        }
                                    }
                            FriendItem(friend = friend, amountOwed = amountOwed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(label: String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppDark),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color.White, fontSize = 16.sp)
            Text(
                "RM%.2f".format(amount),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PendingBillItem(bill: Bill, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppWhite),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bill.category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(bill.category.icon, null, tint = bill.category.color)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bill.description, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Text(bill.date, fontSize = 12.sp, color = Color.Gray)
            }
            Text("RM%.2f".format(bill.amount), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
        }
        // This part is simplified from the image as we don't have split details per bill
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.People, contentDescription = "People", tint = Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            if (bill.paidBy == "You") {
                val names = if (bill.splitWith.isEmpty()) emptyList() else bill.splitWith.split(", ")
                val paidNames = if (bill.paidFriends.isEmpty()) emptyList() else bill.paidFriends.split(", ")
                val share = bill.amount / (names.size + (if (bill.includeMe) 1 else 0))
                val unpaidCount = names.count { !paidNames.contains(it) }
                val currentOwed = share * unpaidCount

                Text("You are owed", color = AppGreen, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("RM%.2f".format(currentOwed), color = AppGreen, fontWeight = FontWeight.Bold)
            } else {
                Text("You owe", color = AppRed, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("RM%.2f".format(bill.amount / 2), color = AppRed, fontWeight = FontWeight.Bold) // Example split
            }

        }
    }
}

@Composable
fun FriendItem(friend: Friend, amountOwed: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Friend", tint = TextDark, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(friend.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.LightGray.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "RM${"%.2f".format(amountOwed)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amountOwed > 0) AppGreen else Color.Gray
                    )
                }
            }
            if (amountOwed > 0) {
                Text("Owes you", fontSize = 13.sp, color = AppGreen)
            } else {
                Text("Settled up", fontSize = 13.sp, color = Color.Gray)
            }
        }
        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
    }
}


// --- BILLS LIST ---
@Composable
fun BillsScreen(
    bills: List<Bill>,
    onDelete: (Bill) -> Unit,
    navController: NavHostController
) {
    var selectedCategory by remember { mutableStateOf<BillCategory?>(null) }
    val filteredBills = if (selectedCategory == null) bills else bills.filter { it.category == selectedCategory }

    Column(modifier = Modifier
        .fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        Text("Expense History", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppWhite, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(40.dp, 40.dp, 0.dp, 0.dp),
            colors = CardDefaults.cardColors(containerColor = AppWhite),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        onClick = { selectedCategory = null },
                        label = { Text("All") },
                        selected = selectedCategory == null
                    )
                    BillCategory.entries.forEach { cat ->
                        FilterChip(
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name) },
                            selected = selectedCategory == cat
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                if (filteredBills.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "opps no one owe you for now",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredBills, key = { it.id }) { bill ->
                            BillItemStyled(
                                bill = bill,
                                onClick = { navController.navigate(Screen.BillDetail.createRoute(bill.id)) },
                                onDelete = { onDelete(bill) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillItemStyled(bill: Bill, onClick: () -> Unit, onDelete: () -> Unit) {
    var isVisible by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    val offsetX by animateDpAsState(targetValue = if (isDeleting) (-80).dp else 0.dp, label = "deleteSlide")

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(300)
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = slideOutVertically(animationSpec = tween(300))
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            // Delete Background
            Card(
                modifier = Modifier.matchParentSize(),
                colors = CardDefaults.cardColors(containerColor = AppRed),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 24.dp).clickable { isVisible = false }
                    )
                }
            }

            // Foreground Content
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5F0)),
                modifier = Modifier
                    .offset(x = offsetX)
                    .fillMaxWidth()
                    .clickable { if (!isDeleting) onClick() }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bill.category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(bill.category.icon, null, tint = bill.category.color)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(bill.description, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        Text(bill.date, fontSize = 12.sp, color = Color.Gray)
                    }
                    
                    // Amount or Settled text
                    Box(modifier = Modifier
                        .clickable { isDeleting = !isDeleting }
                        .padding(8.dp)
                    ) {
                        if (bill.isSettled) {
                            Text("Settled", color = AppGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("RM${"%.2f".format(bill.amount)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplitBillScreen(
    friends: List<Friend>,
    onCancel: () -> Unit,
    onSave: (Bill) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BillCategory.OTHER) }
    var selectedFriends by remember { mutableStateOf(setOf<Int>()) }
    var includeMe by remember { mutableStateOf(true) }
    var isCardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isCardVisible = true
    }

    val totalAmount = amount.toDoubleOrNull() ?: 0.0
    val personCount = selectedFriends.size + (if (includeMe) 1 else 0)
    val splitAmount = if (personCount > 0) totalAmount / personCount else 0.0

    Column(modifier = Modifier
        .fillMaxSize()
        .background(AppOrange)
        .padding(16.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel, modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppWhite)
            }
            Spacer(Modifier.width(16.dp))
            Text("Split Expense", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = AppWhite)
        }

        AnimatedVisibility(
            visible = isCardVisible,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(500)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500))
        ) {
            Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = AppWhite), modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())) {
                    Text("Total Amount", color = Color.Gray, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("RM", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = AppDark)
                        BasicTextField(
                            value = amount,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, color = AppDark),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (totalAmount > 0) {
                        Spacer(Modifier.height(16.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = AppGreen.copy(alpha = 0.15f)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Per Person", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("RM${"%.2f".format(splitAmount)} per person", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(text = "RM${"%.2f".format(splitAmount)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("What is this for?") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Category", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf(BillCategory.GROCERIES, BillCategory.UTILITIES, BillCategory.RENT, BillCategory.OTHER).forEach { cat ->
                            CategoryItem(cat, selectedCategory == cat) { selectedCategory = cat }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Split with", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeMe = !includeMe }, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppDark), contentAlignment = Alignment.Center) {
                            Text("Me", color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Include yourself", modifier = Modifier.weight(1f))
                        Switch(checked = includeMe, onCheckedChange = { includeMe = it })
                    }

                    friends.forEach { friend ->
                        val isSelected = selectedFriends.contains(friend.id)
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                selectedFriends =
                                    if (isSelected) selectedFriends - friend.id else selectedFriends + friend.id
                            }, verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = "Friend", tint = TextDark, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(friend.name, modifier = Modifier.weight(1f))
                            Checkbox(checked = isSelected, onCheckedChange = { selectedFriends = if (it) selectedFriends + friend.id else selectedFriends - friend.id })
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    Button(onClick = {
                        if (totalAmount > 0 && description.isNotBlank()) {
                            val selectedNames = friends.filter { selectedFriends.contains(it.id) }.joinToString(", ") { it.name }
                            onSave(Bill(description = description, amount = totalAmount, paidBy = "You", date = "Today", category = selectedCategory, splitWith = selectedNames, includeMe = includeMe))
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = AppDark), shape = RoundedCornerShape(16.dp)) {
                        Text("Save Split Bill", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- DETAIL SCREEN ---
@Composable
fun BillDetailScreen(
    billId: Int,
    bills: List<Bill>,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onSettle: (Bill) -> Unit,
    onUpdate: (Bill) -> Unit
) {
    val bill = bills.find { it.id == billId } ?: return
    var isCardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isCardVisible = true
    }

    val names = if (bill.splitWith.isEmpty()) emptyList() else bill.splitWith.split(", ")
    val paidFriendsList = if (bill.paidFriends.isEmpty()) emptyList() else bill.paidFriends.split(", ").toMutableList()
    val share = bill.amount / (names.size + (if (bill.includeMe) 1 else 0))
    
    val unpaidCount = names.count { !paidFriendsList.contains(it) }
    val amountStillOwed = share * unpaidCount

    Column(modifier = Modifier
        .fillMaxSize()
        .background(AppOrange)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppWhite) }
            Text(bill.description, color = AppWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onHome) { Icon(Icons.Default.Home, contentDescription = "Home", tint = AppWhite) }
            Surface(shape = CircleShape, color = AppWhite.copy(0.3f), modifier = Modifier.size(44.dp)) {
                Icon(bill.category.icon, null, tint = AppWhite, modifier = Modifier.padding(8.dp))
            }
        }

        AnimatedVisibility(
            visible = isCardVisible,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(500)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500))
        ) {
            Card(shape = RoundedCornerShape(40.dp, 40.dp, 0.dp, 0.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5F0)), modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppWhite), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (bill.paidBy == "You") "You are owed" else "You owe", fontSize = 14.sp, color = Color.Gray)
                                    Text("RM${"%.2f".format(if (bill.paidBy == "You") amountStillOwed else bill.amount / 2)}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (bill.paidBy == "You") AppGreen else AppRed)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    if (!bill.isSettled) {
                                        Button(
                                            onClick = { onSettle(bill) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AppDark),
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier.fillMaxWidth(1f)
                                        ) {
                                            Text("Settle All", color = AppWhite, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {},
                                            enabled = false,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier.fillMaxWidth(1f)
                                        ) {
                                            Text("Settled", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            if (bill.isSettled) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppGreen.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "Settled",
                                        color = AppGreen,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Text("Who owes whom?", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
                        if (names.isNotEmpty()) {
                            names.forEach { name ->
                                val isPaid = paidFriendsList.contains(name)
                                FriendDebtRow(
                                    name = name, 
                                    amount = "${"%.2f".format(share)}",
                                    isPaid = isPaid,
                                    onPaidChange = { checked ->
                                        val newList = paidFriendsList.toMutableList()
                                        if (checked) {
                                            if (!newList.contains(name)) newList.add(name)
                                        } else {
                                            newList.remove(name)
                                        }
                                        onUpdate(bill.copy(paidFriends = newList.joinToString(", ")))
                                    }
                                )
                            }
                        } else {
                            Text("No friends selected for splitting.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    item {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(bill.date, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(20.dp), tint = AppDark)
                        }
                    }
                    item {
                        DetailListItem(bill.description, bill.date, "RM${"%.2f".format(bill.amount)}", if (bill.paidBy == "You") "Owes you" else "You Owe", if (bill.paidBy == "You") AppGreen else AppRed, if (bill.paidBy == "You") Color(0xFFE8F5E9) else Color(0xFFFFEBEB))
                    }
                }
            }
        }
    }
}

// --- HELPERS ---
@Composable
fun DetailListItem(title: String, date: String, amount: String, statusText: String, statusColor: Color, bgColor: Color) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = bgColor), modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Text(text = date, fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (statusText.isNotEmpty()) Text(text = statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                Text(text = amount, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
            }
        }
    }
}

@Composable
fun FriendDebtRow(name: String, amount: String, isPaid: Boolean, onPaidChange: (Boolean) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isPaid) Color.Gray else AppOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    name, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isPaid) Color.Gray else TextDark,
                    textDecoration = if (isPaid) TextDecoration.LineThrough else null
                )
                Text(
                    if (isPaid) "Paid RM$amount" else "Owes RM$amount", 
                    fontSize = 12.sp, 
                    color = if (isPaid) AppGreen else Color.Gray
                )
            }
        }
        Checkbox(
            checked = isPaid, 
            onCheckedChange = onPaidChange,
            colors = CheckboxDefaults.colors(checkedColor = AppGreen)
        )
    }
}

@Composable
fun CategoryItem(category: BillCategory, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .clickable(onClick = onClick)
        .padding(4.dp)) {
        Box(modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) category.color else Color.Gray.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(category.icon, null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(28.dp))
        }
        Text(category.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = if (isSelected) AppDark else Color.Gray)
    }
}

@Composable
fun ChoresScreen(chores: List<Chore>, friends: List<Friend>, onToggle: (Chore) -> Unit, onDelete: (Chore) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        Text("House Chores", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppWhite, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(40.dp, 40.dp, 0.dp, 0.dp),
            colors = CardDefaults.cardColors(containerColor = AppWhite),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
            ) {
                items(chores, key = { it.id }) { chore ->
                    var isVisible by remember { mutableStateOf(true) }

                    LaunchedEffect(isVisible) {
                        if (!isVisible) {
                            delay(300)
                            onDelete(chore)
                        }
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5F0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = chore.isDone, onCheckedChange = { onToggle(chore.copy(isDone = it)) })
                                    Text(
                                        chore.description,
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (chore.isDone) TextDecoration.LineThrough else null,
                                        color = if (chore.isDone) Color.Gray else TextDark
                                    )
                                    IconButton(onClick = { isVisible = false }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Chore", tint = Color.Gray)
                                    }
                                }
                                if (chore.assignedTo != "Me") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 32.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(AppOrange),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                chore.assignedTo.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(chore.assignedTo, fontSize = 14.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsScreen(friends: List<Friend>, bills: List<Bill>, onDelete: (Friend) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        Text("Friends", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppWhite, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(40.dp, 40.dp, 0.dp, 0.dp),
            colors = CardDefaults.cardColors(containerColor = AppWhite),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
            ) {
                items(friends, key = { it.id }) { friend ->
                    val amountOwed = bills.filter { it.paidBy == "You" && !it.isSettled && it.splitWith.split(", ").contains(friend.name) }
                        .sumOf { bill ->
                            val names = bill.splitWith.split(", ")
                            val paidNames = if (bill.paidFriends.isEmpty()) emptyList() else bill.paidFriends.split(", ")
                            if (paidNames.contains(friend.name)) {
                                0.0
                            } else {
                                bill.amount / (names.size + (if (bill.includeMe) 1 else 0))
                            }
                        }
                    var isVisible by remember { mutableStateOf(true) }
                    var isDeleting by remember { mutableStateOf(false) }
                    val offsetX by animateDpAsState(targetValue = if (isDeleting) (-80).dp else 0.dp, label = "deleteSlide")

                    LaunchedEffect(isVisible) {
                        if (!isVisible) {
                            delay(300) // Animation duration
                            onDelete(friend)
                        }
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        exit = slideOutVertically(animationSpec = tween(durationMillis = 300))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            // Delete Background
                            Card(
                                modifier = Modifier.matchParentSize(),
                                colors = CardDefaults.cardColors(containerColor = AppRed),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                                    Text(
                                        "Delete",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 24.dp).clickable { isVisible = false }
                                    )
                                }
                            }

                            // Foreground Content
                            Card(
                                modifier = Modifier
                                    .offset(x = offsetX)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5F0))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(friend.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = TextDark)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(friend.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                                            Spacer(Modifier.weight(1f))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.LightGray.copy(alpha = 0.4f),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "Owes you: ",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "RM${"%.2f".format(amountOwed)}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (amountOwed > 0) AppGreen else Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    IconButton(onClick = { isDeleting = !isDeleting }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Friend", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyFloatingBottomBar(navController: NavHostController, onAddClick: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf(Screen.Dashboard, Screen.Bills, Screen.Friends, Screen.Chores)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(AppWhite),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
            shadowElevation = 30.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First two items
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    items.take(2).forEach { screen ->
                        BottomNavItemRedesign(screen, currentRoute == screen.route) {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                }

                // Spacer for the center button
                Spacer(modifier = Modifier.width(72.dp))

                // Last two items
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    items.drop(2).forEach { screen ->
                        BottomNavItemRedesign(screen, currentRoute == screen.route) {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                }
            }
        }

        // Integrated Add Button (Elevated)
        FloatingActionButton(
            onClick = onAddClick,
            shape = CircleShape,
            containerColor = AppDark,
            contentColor = AppOrange,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .offset(y = (-32.dp))
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun BottomNavItemRedesign(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        screen.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AppOrange else Color.Gray,
                modifier = Modifier.size(26.dp)
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AppOrange)
            )
        }
    }
}


@Composable
fun AddChoreDialog(friends: List<Friend>, onDismiss: () -> Unit, onAdd: (Chore) -> Unit) {
    var text by remember { mutableStateOf("") }
    var selectedFriend by remember { mutableStateOf<String>("Me") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppWhite)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("New Chore", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Assign to:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedFriend = "Me" }) {
                            RadioButton(selected = selectedFriend == "Me", onClick = { selectedFriend = "Me" })
                            Text("Me")
                        }
                    }
                    items(friends) { friend ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedFriend = friend.name }) {
                            RadioButton(selected = selectedFriend == friend.name, onClick = { selectedFriend = friend.name })
                            Text(friend.name)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onAdd(Chore(description = text, assignedTo = selectedFriend, isDone = false)) }, modifier = Modifier.fillMaxWidth()) { Text("Add") }
            }
        }
    }
}

@Composable
fun AddFriendDialog(onDismiss: () -> Unit, onAdd: (Friend) -> Unit) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppWhite)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("New Friend", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Friend's Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onAdd(Friend(name = text)) }, modifier = Modifier.fillMaxWidth()) { Text("Add") }
            }
        }
    }
}
