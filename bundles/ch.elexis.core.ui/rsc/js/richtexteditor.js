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

	function addPageStyle(css) {
		try {
			var style = document.createElement('style');
			style.appendChild(document.createTextNode(css));
			document.head.appendChild(style);
		} catch (e) {
			// best effort
		}
	}

	var NON_MODAL_DIALOGS = { find: true, elexisLineHeight: true };

	addPageStyle('.elexis-dialog-nonmodal .cke_dialog_background_cover{display:none !important;}'
			+ '.elexis-dialog-nonmodal .cke_dialog_body{border:1px solid #a0a0a0 !important;'
			+ 'border-radius:3px;box-shadow:0 3px 14px rgba(0,0,0,0.3) !important;}');

	function setNonModal(on) {
		try {
			var root = document.documentElement;
			if (on) {
				root.classList.add('elexis-dialog-nonmodal');
			} else {
				root.classList.remove('elexis-dialog-nonmodal');
			}
		} catch (e) {
			// best effort
		}
	}

	CKEDITOR.on('instanceReady', function(ev) {
		var editor = ev.editor;
		var lastSelectedText = '';

		editor.setKeystroke(CKEDITOR.CTRL + 70 /* F */, 'find');

		editor.on('selectionChange', function() {
			try {
				var selection = editor.getSelection();
				lastSelectedText = selection ? (selection.getSelectedText() || '') : '';
			} catch (e) {
				lastSelectedText = '';
			}
		});

		editor.on('dialogShow', function(evt) {
			var dialog = evt.data;
			var name = dialog && dialog.getName ? dialog.getName() : '';
			setNonModal(NON_MODAL_DIALOGS[name] === true);
			if (name === 'find') {
				prefillSearch(dialog, lastSelectedText);
			}
		});
		editor.on('dialogHide', function() {
			setNonModal(false);
		});
	});

	function prefillSearch(dialog, text) {
		if (!text) {
			return;
		}
		var currentTab = dialog._ ? dialog._.currentTabId : null;
		var fields = [ [ 'find', 'txtFindFind' ], [ 'replace', 'txtFindReplace' ] ];
		for (var i = 0; i < fields.length; i++) {
			try {
				var field = dialog.getContentElement(fields[i][0], fields[i][1]);
				if (field) {
					field.setValue(text);
					if (fields[i][0] === currentTab) {
						field.select();
					}
				}
			} catch (e) {
				// best effort
			}
		}
	}

	var LINE_HEIGHT_MIN = 0.1;
	var LINE_HEIGHT_MAX = 4;
	var LINE_HEIGHT_BLOCKS = 'p,div,li,h1,h2,h3,h4,h5,h6';

	var ICON_INK = '#333333';

	function svgIcon(body) {
		return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(
				'<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16">' + body + '</svg>');
	}

	var LINE_HEIGHT_ICON = svgIcon('<g fill="' + ICON_INK + '">'
			+ '<rect x="6" y="2" width="9" height="2"/><rect x="6" y="7" width="9" height="2"/>'
			+ '<rect x="6" y="12" width="9" height="2"/>'
			+ '<path d="M3 1 5 4 1 4Z"/><rect x="2.3" y="3.5" width="1.4" height="9"/>'
			+ '<path d="M3 15 5 12 1 12Z"/></g>');

	function allBlocks(editor) {
		try {
			var editable = editor.editable();
			return editable ? editable.$.querySelectorAll(LINE_HEIGHT_BLOCKS) : [];
		} catch (e) {
			return [];
		}
	}

	function lineNodes(block) {
		var lines = [];
		var current = [];
		for (var child = block.firstChild; child; child = child.nextSibling) {
			if (isLineBreak(child)) {
				lines.push(current);
				current = [];
			} else {
				current.push(child);
			}
		}
		lines.push(current);
		return lines;
	}

	function isLineBreak(node) {
		return node.nodeType === 1 && node.nodeName.toLowerCase() === 'br';
	}

	function lineBreaks(block) {
		var breaks = [];
		for (var child = block.firstChild; child; child = child.nextSibling) {
			if (isLineBreak(child)) {
				breaks.push(child);
			}
		}
		return breaks;
	}

	function wholeBlock(block) {
		return { block: block, from: 0, to: lineNodes(block).length - 1 };
	}

	function nativeRanges(ranges, doc) {
		var natives = [];
		for (var i = 0; i < ranges.length; i++) {
			try {
				var range = doc.createRange();
				range.setStart(ranges[i].startContainer.$, ranges[i].startOffset);
				range.setEnd(ranges[i].endContainer.$, ranges[i].endOffset);
				natives.push(range);
			} catch (e) {
				// best effort
			}
		}
		return natives;
	}

	function nodesMarked(nodes, ranges) {
		if (!nodes.length) {
			return false;
		}
		var line = nodes[0].ownerDocument.createRange();
		line.setStartBefore(nodes[0]);
		line.setEndAfter(nodes[nodes.length - 1]);
		for (var i = 0; i < ranges.length; i++) {
			if (ranges[i].compareBoundaryPoints(line.END_TO_START, line) < 0
					&& ranges[i].compareBoundaryPoints(line.START_TO_END, line) > 0) {
				return true;
			}
		}
		return false;
	}

	function markedRegion(block, ranges) {
		var lines = lineNodes(block);
		var from = -1;
		var to = -1;
		for (var i = 0; i < lines.length; i++) {
			if (nodesMarked(lines[i], ranges)) {
				if (from < 0) {
					from = i;
				}
				to = i;
			}
		}
		if (from < 0) {
			return wholeBlock(block);
		}
		return { block: block, from: from, to: to };
	}

	function markedRegions(editor) {
		var regions = [];
		try {
			var editable = editor.editable();
			var body = editable ? editable.$ : null;
			var selection = editor.getSelection(1);
			var ranges = selection ? selection.getRanges() : null;
			if (!ranges || !ranges.length) {
				return regions;
			}
			var marked = false;
			for (var i = 0; i < ranges.length; i++) {
				if (!ranges[i].collapsed) {
					marked = true;
					break;
				}
			}
			if (!marked) {
				return regions;
			}
			var natives = nativeRanges(ranges, body ? body.ownerDocument : document);
			for (var j = 0; j < ranges.length; j++) {
				var iterator = ranges[j].createIterator();
				iterator.enlargeBr = true;
				var block;
				while ((block = iterator.getNextParagraph())) {
					if (block.$ !== body) {
						regions.push(markedRegion(block.$, natives));
					}
				}
			}
		} catch (e) {
			// best effort
		}
		return regions;
	}

	function readLineHeight(regions) {
		for (var i = 0; i < regions.length; i++) {
			var value = parseFloat(regions[i].block.style.lineHeight);
			if (!isNaN(value)) {
				return value;
			}
		}
		return 1;
	}

	function parseLineHeight(raw) {
		var value = parseFloat(String(raw === null || raw === undefined ? '' : raw).replace(',', '.'));
		if (isNaN(value) || value < LINE_HEIGHT_MIN || value > LINE_HEIGHT_MAX) {
			return null;
		}
		return value;
	}

	function splitAfter(block, lineBreak) {
		var rest = block.cloneNode(false);
		rest.removeAttribute('id');
		var node = lineBreak.nextSibling;
		while (node) {
			var next = node.nextSibling;
			rest.appendChild(node);
			node = next;
		}
		lineBreak.parentNode.removeChild(lineBreak);
		block.parentNode.insertBefore(rest, block.nextSibling);
		keepVisible(block);
		keepVisible(rest);
		return rest;
	}

	function keepVisible(block) {
		if (!block.firstChild) {
			block.appendChild(block.ownerDocument.createElement('br'));
		}
	}

	function isolate(region) {
		var block = region.block;
		if (region.from <= 0 && region.to >= lineNodes(block).length - 1) {
			return block;
		}
		var target = block;
		if (region.from > 0) {
			var breaks = lineBreaks(block);
			if (breaks.length < region.from) {
			return null;
			}
			target = splitAfter(block, breaks[region.from - 1]);
		}
		var rest = lineBreaks(target);
		var marked = region.to - region.from;
		if (rest.length > marked) {
			splitAfter(target, rest[marked]);
		}
		return target;
	}

	function applyLineHeight(editor, value, regions) {
		if (!regions || !regions.length) {
			return;
		}
		editor.fire('saveSnapshot');
		for (var i = 0; i < regions.length; i++) {
			var block = regions[i].block;
			if (block && block.isConnected !== false) {
				var target = isolate(regions[i]);
				if (target) {
					target.style.lineHeight = String(value);
				}
			}
		}
		editor.fire('saveSnapshot');
	}

	CKEDITOR.plugins.add('elexisspacing', {
		init: function(editor) {
			var marked = [];

			function trackFromEditable() {
				marked = markedRegions(editor);
			}

			editor.on('selectionChange', function() {
				var regions = markedRegions(editor);
				if (regions.length) {
					marked = regions;
				}
			});

			editor.on('contentDom', function() {
				try {
					var doc = editor.document ? editor.document.$ : null;
					if (doc) {
						doc.addEventListener('mouseup', trackFromEditable, true);
						doc.addEventListener('keyup', trackFromEditable, true);
					}
				} catch (e) {
					// best effort
				}
			});

			function targetRegions() {
				if (marked.length) {
					return marked;
				}
				var blocks = allBlocks(editor);
				var regions = [];
				for (var i = 0; i < blocks.length; i++) {
					regions.push(wholeBlock(blocks[i]));
				}
				return regions;
			}

			CKEDITOR.dialog.add('elexisLineHeight', function() {
				return {
					title: 'Zeilenabstand',
					minWidth: 300,
					minHeight: 60,
					contents: [ {
						id: 'tab',
						label: 'Zeilenabstand',
						elements: [ {
							type: 'text',
							id: 'value',
							label: 'Faktor zwischen ' + LINE_HEIGHT_MIN + ' und ' + LINE_HEIGHT_MAX
									+ ', z. B. 1.5',
							setup: function() {
								this.setValue(String(readLineHeight(targetRegions())));
							},
							validate: function() {
								return parseLineHeight(this.getValue()) === null
										? 'Bitte eine Zahl zwischen ' + LINE_HEIGHT_MIN + ' und '
												+ LINE_HEIGHT_MAX + ' eingeben.'
										: true;
							},
							commit: function() {
								applyLineHeight(editor, parseLineHeight(this.getValue()), targetRegions());
							}
						} ]
					} ],
					onShow: function() {
						var regions = markedRegions(editor);
						if (regions.length) {
							marked = regions;
						}
						this.setupContent();
					},
					onOk: function() {
						this.commitContent();
					}
				};
			});
			editor.addCommand('elexisLineHeight', new CKEDITOR.dialogCommand('elexisLineHeight'));
			editor.ui.addButton('ElexisLineHeight', {
				label: 'Zeilenabstand',
				title: 'Zeilenabstand',
				command: 'elexisLineHeight',
				toolbar: 'paragraph,100'
			});
		}
	});

	function findButton(editor, name) {
		var found = null;
		try {
			var instance = editor.ui.instances ? editor.ui.instances[name] : null;
			var id = instance && instance._ ? instance._.id : null;
			found = id ? document.getElementById(id) : null;
			if (!found) {
				found = document.querySelector('.cke_button__' + name.toLowerCase());
			}
			if (!found && instance && instance.title) {
				var buttons = document.querySelectorAll('a.cke_button');
				for (var i = 0; i < buttons.length; i++) {
					if (buttons[i].getAttribute('title') === instance.title) {
						found = buttons[i];
						break;
					}
				}
			}
		} catch (e) {
			// best effort
		}
		return found;
	}

	function iconElement(button) {
		if (!button) {
			return null;
		}
		var icon = button.querySelector('.cke_button_icon');
		if (!icon) {
			icon = document.createElement('span');
			icon.className = 'cke_button_icon';
			button.insertBefore(icon, button.firstChild);
		}
		return icon;
	}

	function setButtonIcon(button, dataUri) {
		var icon = iconElement(button);
		if (!icon || icon.getAttribute('data-elexis-icon') === dataUri) {
			return;
		}
		icon.style.backgroundImage = 'url("' + dataUri + '")';
		icon.style.backgroundPosition = '0 0';
		icon.style.backgroundSize = '16px';
		icon.setAttribute('data-elexis-icon', dataUri);
	}

	CKEDITOR.on('instanceReady', function(ev) {
		setButtonIcon(findButton(ev.editor, 'ElexisLineHeight'), LINE_HEIGHT_ICON);
	});

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
