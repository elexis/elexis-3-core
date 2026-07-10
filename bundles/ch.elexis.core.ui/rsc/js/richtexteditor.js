/*******************************************************************************
 * CKEditor customizations for RichTextEditorComposite.
 *
 * Executed once in the browser widget before initEditor() is called. Reads its
 * configuration from window.elexisRichTextConfig:
 *   paragraphSpacing - CSS margin below paragraphs (e.g. '2px')
 *   plainDisplay     - hide toolbar and character formatting (structure kept)
 *
 * Everything is hooked on CKEditor's instanceReady event, so no timers on the
 * Java side are needed: the document is revealed and the editor height is
 * maximized as soon as the editor is actually ready.
 ******************************************************************************/
(function() {
	'use strict';

	var cfg = window.elexisRichTextConfig || {};

	function addContentStyle(editor, css) {
		try {
			var doc = editor.document.$;
			var head = doc.head || doc.getElementsByTagName('head')[0];
			if (!head) {
				return;
			}
			var style = doc.createElement('style');
			style.appendChild(doc.createTextNode(css));
			head.appendChild(style);
		} catch (e) {
			// best effort
		}
	}

	CKEDITOR.on('instanceReady', function(ev) {
		addContentStyle(ev.editor,
				'@media print{*{-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;}}');
		addContentStyle(ev.editor,
				'p{margin-top:0 !important;margin-bottom:' + (cfg.paragraphSpacing || '2px') + ' !important;}');
		if (cfg.plainDisplay) {
			addContentStyle(ev.editor,
					'body,body *{font-weight:normal !important;font-style:normal !important;'
					+ 'text-decoration:none !important;color:inherit !important;'
					+ 'background-color:transparent !important;font-family:inherit !important;'
					+ 'font-size:inherit !important;text-align:left !important;}'
					+ 'p,div{text-indent:0 !important;margin-left:0 !important;padding-left:0 !important;}');
			try {
				var style = document.createElement('style');
				style.appendChild(document.createTextNode('.cke_top,.cke_bottom{display:none !important;}'));
				document.head.appendChild(style);
			} catch (e) {
				// best effort
			}
		}
		try {
			document.documentElement.style.visibility = 'visible';
		} catch (e) {
			// best effort
		}
		if (typeof maximizeEditorHeight === 'function') {
			maximizeEditorHeight();
		}
	});

	CKEDITOR.on('instanceReady', function(ev) {
		ev.editor.on('key', function(evt) {
			if (evt.data && evt.data.keyCode === 13) {
				evt.stop();
			}
		}, null, null, 1);
	});

	CKEDITOR.on('instanceReady', function(ev) {
		var editor = ev.editor;
		var pendingLi = null;

		function caretBlock() {
			var sel = editor.getSelection();
			if (!sel) {
				return null;
			}
			var r = sel.getRanges()[0];
			if (!r || !r.collapsed) {
				return null;
			}
			return r.startPath().block;
		}

		editor.on('key', function(evt) {
			var key = evt.data.keyCode;
			if (key === 32) {
				setTimeout(function() {
					try {
						var block = caretBlock();
						if (!block || block.getName() === 'li') {
							return;
						}
						var text = (block.getText() || '').replace(/\u00a0/g, ' ').trim();
						if (text !== '-') {
							return;
						}
						editor.execCommand('bulletedlist');
						var start = editor.getSelection().getStartElement();
						var li = start ? (start.getName() === 'li' ? start : start.getAscendant('li', true)) : null;
						if (li) {
							li.setHtml('');
							var r = editor.createRange();
							r.moveToElementEditStart(li);
							editor.getSelection().selectRanges([r]);
							pendingLi = li;
						}
					} catch (e) {
						// best effort
					}
				}, 0);
			} else if (key === 8) {
				try {
					var block = caretBlock();
					if (!block || block.getName() !== 'li' || !pendingLi) {
						return;
					}
					if (!block.equals(pendingLi)) {
						return;
					}
					var text = (block.getText() || '').replace(/\u00a0/g, ' ').trim();
					if (text !== '') {
						return;
					}
					evt.cancel();
					editor.execCommand('bulletedlist');
					editor.insertText('- ');
					pendingLi = null;
				} catch (e) {
					// best effort
				}
			}
		});
	});

	setTimeout(function() {
		try {
			document.documentElement.style.visibility = 'visible';
		} catch (e) {
			// best effort
		}
	}, 3000);
})();
