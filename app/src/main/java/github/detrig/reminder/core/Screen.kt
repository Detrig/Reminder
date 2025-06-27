package github.detrig.reminder.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

interface Screen {

    fun show(supportFragmentManager: FragmentManager, containerId: Int)

    abstract class Replace(
        private val fragmentClass: Class<out Fragment>,
        private val args: Bundle? = null
    ) : Screen {
        override fun show(supportFragmentManager: FragmentManager, containerId: Int) {
            val fragment = fragmentClass.getDeclaredConstructor().newInstance().apply {
                arguments = args
            }
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(fragmentClass.name)
                .replace(containerId, fragment)
                .commit()
        }
    }

    abstract class Add(private val fragment : Fragment) : Screen {
        override fun show(supportFragmentManager: FragmentManager, containerId: Int) {
            supportFragmentManager
                .beginTransaction()
                .add(containerId, fragment)
                .addToBackStack(fragment::class.java.name)
                .commit()
        }
    }

    abstract class ReplaceMain(private val fragmentClass: Class<out Fragment>) : Screen {
        override fun show(supportFragmentManager: FragmentManager, containerId: Int) {

            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            supportFragmentManager
                .beginTransaction()
                .replace(containerId, fragmentClass.getDeclaredConstructor().newInstance())
                .commit()
        }
    }

    object Pop : Screen {
        override fun show(supportFragmentManager: FragmentManager, containerId: Int) {
            supportFragmentManager.popBackStack()
        }
    }
}