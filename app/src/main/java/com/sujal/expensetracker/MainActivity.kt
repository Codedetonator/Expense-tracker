package com.sujal.expensetracker

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sujal.expensetracker.databinding.ActivityMainBinding
import com.sujal.expensetracker.ui.add.AddExpenseBottomSheet
import com.sujal.expensetracker.ui.expenses.ExpensesFragment
import com.sujal.expensetracker.ui.home.HomeFragment
import com.sujal.expensetracker.ui.lending.LendingFragment
import com.sujal.expensetracker.ui.wallet.WalletFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedTab = TAB_HOME

    private val tabViews: List<LinearLayout>
        get() = listOf(binding.tabHome, binding.tabExpenses, binding.tabWallet, binding.tabLending)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedTab = savedInstanceState?.getInt(KEY_TAB) ?: TAB_HOME

        tabViews.forEachIndexed { index, tab -> tab.setOnClickListener { selectTab(index) } }
        binding.fabAddExpense.setOnClickListener {
            AddExpenseBottomSheet().show(supportFragmentManager, "add_expense")
        }

        highlightTab(selectedTab)
        if (savedInstanceState == null) showFragment(fragmentFor(selectedTab))

        handleAddSheetIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAddSheetIntent(intent)
    }

    /** Opens the quick-add sheet when launched from the home-screen widget's + button. */
    private fun handleAddSheetIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_ADD_SHEET, false) == true) {
            intent.removeExtra(EXTRA_OPEN_ADD_SHEET)
            binding.root.post {
                if (supportFragmentManager.findFragmentByTag("add_expense") == null) {
                    AddExpenseBottomSheet().show(supportFragmentManager, "add_expense")
                }
            }
        }
    }

    /** Switches to a tab. Public so screens (e.g. Home's "See all") can jump to another tab. */
    fun selectTab(index: Int) {
        val alreadyShowing = index == selectedTab &&
            supportFragmentManager.findFragmentById(R.id.fragment_container) != null
        if (alreadyShowing) return
        selectedTab = index
        highlightTab(index)
        showFragment(fragmentFor(index))
    }

    private fun fragmentFor(index: Int): Fragment = when (index) {
        TAB_EXPENSES -> ExpensesFragment()
        TAB_WALLET -> WalletFragment()
        TAB_LENDING -> LendingFragment()
        else -> HomeFragment()
    }

    private fun highlightTab(index: Int) {
        val active = getColor(R.color.primary)
        val inactive = getColor(R.color.text_secondary)
        tabViews.forEachIndexed { i, tab ->
            val color = if (i == index) active else inactive
            (tab.getChildAt(0) as ImageView).imageTintList = ColorStateList.valueOf(color)
            (tab.getChildAt(1) as TextView).apply {
                setTextColor(color)
                setTypeface(null, if (i == index) Typeface.BOLD else Typeface.NORMAL)
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /** Opens a screen that isn't one of the 4 bottom-nav tabs (e.g. Wishlist, Trips, trip
     * detail), keeping it on the back stack so the system back button returns to Home. */
    fun openSubScreen(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, selectedTab)
    }

    companion object {
        const val TAB_HOME = 0
        const val TAB_EXPENSES = 1
        const val TAB_WALLET = 2
        const val TAB_LENDING = 3
        private const val KEY_TAB = "selected_tab"
        const val EXTRA_OPEN_ADD_SHEET = "open_add_sheet"
    }
}
