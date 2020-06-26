package ch.elexis.core.eigenartikel.service;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.eigenartikel.EigenartikelTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.data.Artikel;
import ch.elexis.data.Query;
import ch.rgw.tools.Money;

@Component
public class EigenartikelCodeElementService implements ICodeElementServiceContribution {
	
	@Activate
	public void activate() {
		ExecutorService initExec = Executors.newSingleThreadExecutor();
		initExec.execute(() -> {
			// wait for login
			while(ElexisEventDispatcher.getSelectedMandator() == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			// init covid article
			Query<Eigenartikel> query = new Query<>(Eigenartikel.class);
			String found = query.findSingle(Artikel.FLD_SUB_ID, Query.EQUALS, "3028");
			if (found == null) {
				Eigenartikel product = new Eigenartikel(
						"Ärztliche Pauschale SARS-CoV-2-Test nach Teststrategie BAG",
						"Ärztliche Pauschale SARS-CoV-2-Test nach Teststrategie BAG");
				product.setTyp(EigenartikelTyp.COVID);
				product.set(Eigenartikel.FLD_SUB_ID, "3028");

				Eigenartikel article = new Eigenartikel(
						"Ärztliche Pauschale SARS-CoV-2-Test nach Teststrategie BAG – Pauschale für Ärzte",
						"Ärztliche Pauschale SARS-CoV-2-Test nach Teststrategie BAG – Pauschale für Ärzte");
				article.set(Eigenartikel.FLD_SUB_ID, "3028");
				article.setTyp(EigenartikelTyp.COVID);
				article.setVKPreis(new Money(5000));
				article.set(Eigenartikel.FLD_EXTID, product.getId());
			}
			initExec.shutdown();
		});
	}

	@Override
	public String getSystem(){
		return Eigenartikel.TYPNAME;
	}
	
	@Override
	public Optional<ICodeElement> createFromCode(String code, HashMap<Object, Object> context){
		Query<Eigenartikel> query = new Query<>(Eigenartikel.class);
		String found = query.findSingle(Artikel.FLD_SUB_ID, Query.EQUALS, code);
		if (found != null) {
			return Optional.of(Eigenartikel.load(found));
		} else {
			query.clear();
			found = query.findSingle(Eigenartikel.FLD_ID, Query.EQUALS, code);
			if (found != null) {
				return Optional.of((ICodeElement) Eigenartikel.load(found));
			}
		}
		return Optional.empty();
	}
	
}
