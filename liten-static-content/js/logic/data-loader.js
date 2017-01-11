
import $ from 'jquery';

const NEXT_OPEN_TAG     = '<next>';
const NEXT_CLOSE_TAG    = '</next>';

function appendFadeInHtmlBlock($container, htmlString) {
  // NOTE:  two approaches are possible here, one uses jquery abstractions (and works on pretty old browsers),
  //        while the other uses insertAdjacentHTML, the standard API in all the modern browsers (and old IEs too!).
  //
  // First approach (jquery way):
  //        const $element = $($.parseHTML(htmlString));
  //        $element.appendTo($container);
  //
  // Other approach (pure HTML):
  //        $container.each(function () { this.insertAdjacentHTML('beforeend', htmlString); });
  //
  // Links:
  //    https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentHTML
  //    https://msdn.microsoft.com/en-us/library/ms536452(v=vs.85).aspx

  // Note on use of jquery (in favor of insertAdjacentHTML) - this approach uses fadeIn animation which
  // is easier to implement with jquery
  const $element = $($.parseHTML(htmlString));
  setUpDataLoaderHandlers($element);

  $element.appendTo($container);
  $element.hide().fadeIn();
}

export function parseNextPageUrl(htmlPageString, onNextUrlPresentFn, onNextUrlAbsentFn) {
  // next URL should be appended in HTML comment containing next URL enclosed in 'next' tags,
  // it can look as follows:
  // ...(elements)...<!-- <next>/user/items?offset=100&limit=10</next> -->
  const nextStart = htmlPageString.lastIndexOf(NEXT_OPEN_TAG);
  const nextEnd = htmlPageString.lastIndexOf(NEXT_CLOSE_TAG);
  const nextUrlStart = nextStart + NEXT_OPEN_TAG.length;

  if (nextEnd > nextUrlStart) {
    // there is another page
    const nextUrl = htmlPageString.substring(nextUrlStart, nextEnd);
    onNextUrlPresentFn(nextUrl);
  } else {
    onNextUrlAbsentFn();
  }

}

function fetchAndAppendHtml($list, $loadButton, $loadButtons) {
  const pageUrl = $loadButton.attr('next-url');
  console.log("About to fetch", pageUrl);

  const $deferred = $.ajax(pageUrl);

  $deferred.fail(function () {
    console.warn("Error while loading", pageUrl);
  });

  $deferred.done(function (htmlPageString) {
    //console.log("Retrieved htmlPageString ", htmlPageString, " for url", pageUrl);
    parseNextPageUrl(htmlPageString, (nextUrl) => {
      $loadButton.attr('next-url', nextUrl);
    }, () => {
      console.log("nothing to load, removing...");
      // nothing left to load, disable load buttons
      $loadButtons.remove();
    });

    appendFadeInHtmlBlock($list, htmlPageString);

    // scroll to
    const top = $list.children().last().offset().top;
    $('html, body').animate({
      scrollTop: top
    }, 500);
  });
}

function loadMoreHandlerForElement() {
  const targetListSel = $(this).attr('target-list');
  const $targetList = $(targetListSel);
  if ($targetList.length === 0) {
    console.warn('Target list is missing for load button', this);
    return;
  }

  const loadButtonsClass = $(this).attr('load-button-class');
  const $loadButtons = $('.' + loadButtonsClass);
  if ($loadButtons.length === 0) {
    console.warn('Load buttons class is missing for load button', this);
    return;
  }

  fetchAndAppendHtml($targetList, $(this), $loadButtons);
}

function loadDeferredForElement() {
  const $container = $(this);
  const deferredLoadUrl = $(this).attr('deferred-load-url');
  const $deferred = $.ajax(deferredLoadUrl);

  $deferred.fail(function () {
    console.warn("Error while loading", deferredLoadUrl);
  });

  $deferred.done(function (htmlString) {
    const $element = $($.parseHTML(htmlString));
    setUpDataLoaderHandlers($element);
    $element.appendTo($container);
  });
}

export function setUpDataLoaderHandlers($target) {
  $target = $target || $(document.body);
  //console.log("> target", $target);

  $('.deferred-load', $target).each(loadDeferredForElement);
  // Note: 'on' used here to assign handlers for dynamically created elements
  $('.load-more', $target).click(loadMoreHandlerForElement);
}
