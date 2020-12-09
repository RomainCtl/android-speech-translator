package fr.enssat.babelblock.chantrel_perrot.tools.worker

import timber.log.Timber

/**
 * Method for sleeping for a fixed about of time to emulate slower work
 */
fun sleep(delayInMillis: Long) {
    try {
        Thread.sleep(delayInMillis, 0)
    } catch (e: InterruptedException) {
        Timber.e(e.message)
    }

}