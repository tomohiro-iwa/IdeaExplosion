package CVInternship;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IMindMapDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class IdeaExplosionAction implements IPluginActionDelegate {

	private AstahAPI api;

	public Object run(IWindow window) throws UnExpectedException {
		try {
			this.api = AstahAPI.getAstahAPI();

			IPresentation[] pdata = api.getViewManager().getDiagramViewManager().getSelectedPresentations();

			List<String> relation_words = getRelationWords(pdata[0].getLabel());
			addRelationWords(pdata[0],relation_words);

		} catch (ProjectNotFoundException e) {
			String message = "Project is not opened.Please open the project or create new project.";
			JOptionPane.showMessageDialog(window.getParent(), message, "Warning", JOptionPane.WARNING_MESSAGE);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			System.out.println(sw.toString());
			JOptionPane.showMessageDialog(window.getParent(), "Unexpected error has occurred.", "Alert", JOptionPane.ERROR_MESSAGE);
			throw new UnExpectedException();
		}
		return null;
	}

	private List<String> getRelationWords(String serch) throws Exception
	{
		List<String> relation_words = new ArrayList<String>();

		String wiki_ja = "https://ja.wikipedia.org/wiki/";
		Document word_html = Jsoup.connect(wiki_ja+serch).maxBodySize(0).get();
		Elements cat_tags = word_html.select("#mw-normal-catlinks").select("a");
		System.out.println(word_html.outerHtml());
		Element last_cat_tag = cat_tags.last();

		Document cat_html = Jsoup.connect(wiki_ja + "Category:"+last_cat_tag.text()).get();
		Elements relation_tags = cat_html.select("div .mw-category").select("a");

		for(Element tag : relation_tags)
		{
			relation_words.add(tag.text());
		}
		return relation_words;
	}

	private void addRelationWords(IPresentation targetNode, List<String> words) throws Exception 
	{
		IDiagram diagram = this.api.getViewManager().getDiagramViewManager().getCurrentDiagram();
		ProjectAccessor projectAccessor = this.api.getProjectAccessor();
		if(!(diagram instanceof IMindMapDiagram)) {
			return;
		}
		TransactionManager.beginTransaction();
		for(String s : words) {
			if(s.isEmpty())
				continue;
			projectAccessor.getDiagramEditorFactory().getMindmapEditor().createTopic((INodePresentation)targetNode, s);
		}
		TransactionManager.endTransaction();
	}
}
