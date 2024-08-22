package siosio.kodkod

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.text.CharArrayUtil
import org.jetbrains.kotlin.idea.kdoc.KDocElementFactory
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

class EnterAfterKDocGenHandler : EnterHandlerDelegateAdapter() {

    override fun postProcessEnter(file: PsiFile,
                                  editor: Editor,
                                  dataContext: DataContext): EnterHandlerDelegate.Result {

        if (file !is KtFile || !CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER) {
            return EnterHandlerDelegate.Result.Continue
        }

        val caretModel = editor.caretModel
        if (!isInKDoc(editor, caretModel.offset)) {
            return EnterHandlerDelegate.Result.Continue
        }

        val project = file.project
        val documentManager = PsiDocumentManager.getInstance(project)
        documentManager.commitAllDocuments()

        val elementAtCaret = file.findElementAt(caretModel.offset)
        val kdoc = PsiTreeUtil.getParentOfType(elementAtCaret, KDoc::class.java)
                   ?: return EnterHandlerDelegate.Result.Continue
        val kdocSection = kdoc.getChildOfType<KDocSection>() ?: return EnterHandlerDelegate.Result.Continue
        // KDocのセクションが空(*だけ)以外の場合は処理しない。
        if (kdocSection.text.trim() != "*") {
            return EnterHandlerDelegate.Result.Continue
        }

        val kDocElementFactory = KDocElementFactory(project)
        val parent = kdoc.parent
        val kDocGenerator = when (parent) {
            is KtNamedFunction -> NamedFunctionKDocGenerator(parent)
            is KtClass -> ClassKDocGenerator(parent)
            else -> null
        }
        val application = ApplicationManager.getApplication()
        application.executeOnPooledThread {
            val newKdocText = runReadAction {
                kDocGenerator?.generate()!!
            }
            runInEdt {
                WriteCommandAction
                    .writeCommandAction(project)
                    .withName("Generate KDoc")
                    .run<Throwable> {
                        val newKdoc = kDocElementFactory.createKDocFromText(newKdocText)
                        val replaced = kdoc.replace(newKdoc)
                        val reformatted = CodeStyleManager.getInstance(project).reformat(replaced)
                        reformatted.getChildOfType<KDocSection>()
                            ?.let { caretModel.moveToOffset(it.textOffset + 6) }
                    }
            }
        }
        return EnterHandlerDelegate.Result.Continue
    }

    private fun isInKDoc(editor: Editor, offset: Int): Boolean {
        val document = editor.document
        val docChars = document.charsSequence
        var i = CharArrayUtil.lastIndexOf(docChars, "/**", offset)
        if (i >= 0) {
            i = CharArrayUtil.indexOf(docChars, "*/", i)
            return i > offset
        }
        return false
    }
}
